package choosenearme

import net.liftweb.record.field.{StringField, UniqueIdField, IntField}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}
import net.liftweb.mongodb.record.field.{ObjectIdField}

class City extends MongoRecord[City] with MongoId[City] {
  def meta = City

  object userId extends ObjectIdField(this)
  object name extends StringField(this, 100)
  object latlng extends MongoPoint(this)
  object numCheckins extends IntField(this)
}

object City extends City with MongoMetaRecord[City] {
  
}
