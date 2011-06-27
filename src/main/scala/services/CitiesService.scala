package choosenearme

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JInt, JArray, JDouble}

case class City(name: String, latlng: LatLng)

class CitiesService(userDb: UserDb, foursquare: FoursquareApi) extends RestApiService {
  private def latlngForCheckin(checkin: CheckinDetail): Option[LatLng] =
    checkin.venue.map(v => {
      val loc = v.location
      LatLng(loc.lat, loc.lng)
    })

  def citiesForCheckins(checkins: List[CheckinDetail]): List[(City, Int)] = {
    val latlngs = checkins.flatMap(latlngForCheckin)
    val centroids = LatLng.cluster(latlngs, 25000)

    val clusteredCheckins =
      checkins.groupBy(checkin => {
        for {
          latlng <- latlngForCheckin(checkin)
        } yield centroids.min(LatLng.near(latlng))
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
        (City(cityLabels(ll), ll), cs.size)
      }).toList.sortBy(- _._2)
    
    cityHistogram
  }

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")

    for {
      user <- userDb.fetchOne(User.where(_.secret eqs secret))
      val api = foursquare.authenticateUser(user)
      allCheckins <- api.allCheckins
    } yield {
      val cities = citiesForCheckins(allCheckins)
      val fields =
        for ((city, cityCount) <- cities)
          yield JObject(List(
            JField("name", JString(city.name)),
            JField("lat", JDouble(city.latlng.lat)),
            JField("lng", JDouble(city.latlng.lng)),
            JField("count", JInt(cityCount))))

      new RestApiResponse(JObject(List(JField("response", JArray(fields)))))
    }
  }
}
