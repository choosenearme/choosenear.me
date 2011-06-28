package choosenearme

import net.liftweb.record.field.{StringField, UniqueIdField}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}

class User extends MongoRecord[User] with MongoId[User] {
  def meta = User

  object firstName extends StringField(this, 100)
  object lastName extends StringField(this, 100)
  object email extends StringField(this, 100)
  object phone extends StringField(this, 100)
  object secret extends UniqueIdField(this, 16)

  object foursquareId extends StringField(this, 100)
  object foursquareToken extends StringField(this, 100)
}

object User extends User with MongoMetaRecord[User] {
  
}
