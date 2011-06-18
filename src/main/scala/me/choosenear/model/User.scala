package me.choosenear

import com.foursquare.rogue.AbstractQuery
import com.twitter.util.{Future, FuturePool}
import net.liftweb.record.field.{StringField, UniqueIdField}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}

class User extends MongoRecord[User] with MongoId[User] {
  def meta = User

  object secret extends UniqueIdField(this, 16)
  object foursquareAccessToken extends StringField(this, 100)
}

object User extends User with MongoMetaRecord[User] {
  
}

trait UserDb {
  def fetch[R](q: AbstractQuery[User, R, _, _, _, _]): Future[List[R]]
  def save(user: User): Future[Unit]
}

class MongoUserDb(pool: FuturePool) extends UserDb {
  override def fetch[R](q: AbstractQuery[User, R, _, _, _, _]): Future[List[R]] = pool(q.fetch)
  override def save(user: User): Future[Unit] = pool(user.save)
}
