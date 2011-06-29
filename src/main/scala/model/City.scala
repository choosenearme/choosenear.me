package choosenearme

import com.foursquare.rogue.Degrees
import net.liftweb.record.field.{StringField, UniqueIdField, IntField}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}
import net.liftweb.mongodb.record.field.{ObjectIdField}

class City extends MongoRecord[City] with MongoId[City] {
  def meta = City

  object userId extends StringField(this, 100)
  object name extends StringField(this, 100)
  object latlng extends MongoPoint(this)
  object numCheckins extends IntField(this)
}

object City extends City with MongoMetaRecord[City] {
  val MetersPerDegree = 111111.0
  val CityRadiusInMeters = 25000
  val CityRadiusInDegrees = Degrees(CityRadiusInMeters / MetersPerDegree)

  private def latlngForCheckin(checkin: CheckinDetail): Option[LatLong] =
    checkin.venue.map(v => {
      val loc = v.location
      LatLong(loc.lat, loc.lng)
    })

  def citiesForCheckins(user: User)(checkins: List[CheckinDetail]): List[City] = {
    val latlngs = checkins.flatMap(latlngForCheckin)
    val centroids = LatLongUtil.cluster(latlngs, CityRadiusInMeters)

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
          .userId(user.foursquareId.value)
          .name(cityLabels(ll))
          .latlng(ll)
          .numCheckins(cs.size))
      }).toList.sortBy(- _.numCheckins.value)

    cityHistogram
  }
}
