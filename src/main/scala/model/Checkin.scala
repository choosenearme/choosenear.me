package choosenearme

import org.bson.types.ObjectId
import net.liftweb.record.field.{StringField, UniqueIdField, IntField}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}
import net.liftweb.mongodb.record.field.{ObjectIdField, MongoListField}

class Checkin extends MongoRecord[Checkin] with MongoId[Checkin] {
  def meta = Checkin

  object userId extends StringField(this, 100)
  object venuename extends StringField(this, 100)
  object crossStreet extends StringField(this, 100)
  object latlng extends MongoPoint(this)
  object categories extends MongoListField[Checkin, String](this)
}

object Checkin extends Checkin with MongoMetaRecord[Checkin] {
  def fromCheckinDetail(user: User)(checkinDetail: CheckinDetail): Option[Checkin] = {
    for (venue <- checkinDetail.venue) yield {
      (Checkin
        .createRecord
        ._id(new ObjectId(checkinDetail.id))
        .userId(user.foursquareId.value)
        .venuename(venue.name)
        .crossStreet(venue.location.crossStreet)
        .latlng(LatLong(venue.location.lat, venue.location.lng))
        .categories(CategoryUtil.categoriesForCheckin(checkinDetail)))
    }
  }
}
