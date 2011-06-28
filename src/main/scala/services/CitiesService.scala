package choosenearme

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JInt, JArray, JDouble}

class CitiesService(db: Db, foursquare: FoursquareApi) extends RestApiService {
  private def latlngForCheckin(checkin: CheckinDetail): Option[LatLong] =
    checkin.venue.map(v => {
      val loc = v.location
      LatLong(loc.lat, loc.lng)
    })

  def citiesForCheckins(checkins: List[CheckinDetail]): List[City] = {
    val latlngs = checkins.flatMap(latlngForCheckin)
    val centroids = LatLongUtil.cluster(latlngs, 25000)

    val clusteredCheckins =
      checkins.groupBy(checkin => {
        for {
          latlng <- latlngForCheckin(checkin)
        } yield centroids.min(LatLongUtil.near(latlng))
      })

    val cityNameHistogram =
      clusteredCheckins.mapValues(checkins => {
        val cities =
          for {
            checkin <- checkins
            venue <- checkin.venue
            val loc = venue.location
          } yield loc.city + ", " + loc.state
        (cities
          .groupBy(identity)
          .mapValues(_.size)
          .toList
          .sortBy(- _._2))
      })

    val cityLabels =
      cityNameHistogram.flatMap({case (latlng, cities) =>                                         
        for {
          ll <- latlng
          (city, _) <- cities.headOption
        } yield (ll, city)
      })

    val cityHistogram =
      clusteredCheckins.flatMap({ case (latlng, cs) =>
        for (ll <- latlng) yield (ll, cs)
      }).map({ case (ll, cs) =>
        (City
          .createRecord
          .name(cityLabels(ll))
          .latlng(ll)
          .numCheckins(cs.size))
      }).toList.sortBy(- _.numCheckins.value)

    cityHistogram
  }

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")

    for {
      user <- db.fetchOne(User.where(_.secret eqs secret))
      val api = foursquare.authenticateUser(user)
      allCheckins <- api.allCheckins
      val cities = citiesForCheckins(allCheckins)
      // val _ = cities.foreach(_.userId(user.foursquareId.value))
      // val _ = db.insertAll(cities)
    } yield {
      val fields =
        for (city <- cities)
          yield JObject(List(
            JField("id", JString(city.id.toString)),
            JField("name", JString(city.name.value)),
            JField("lat", JDouble(city.latlng.lat)),
            JField("lng", JDouble(city.latlng.long)),
            JField("count", JInt(city.numCheckins.value))))

      new RestApiResponse(JObject(List(JField("response", JArray(fields)))))
    }
  }
}
