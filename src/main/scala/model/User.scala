package choosenearme

import com.foursquare.rogue.AbstractQuery
import com.twitter.util.{Future, FuturePool}
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

trait UserDb {
  def fetchOne[R](q: AbstractQuery[User, R, _, _, _, _]): Future[R]
  def fetch[R](q: AbstractQuery[User, R, _, _, _, _]): Future[List[R]]
  def save(user: User): Future[User]
}

class MongoUserDb(pool: FuturePool) extends UserDb {
  override def fetchOne[R](q: AbstractQuery[User, R, _, _, _, _]): Future[R] = pool(q.fetch.head)
  override def fetch[R](q: AbstractQuery[User, R, _, _, _, _]): Future[List[R]] = pool(q.fetch)
  override def save(user: User): Future[User] = pool(user.save)
}
