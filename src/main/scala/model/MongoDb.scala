package choosenearme

import com.foursquare.rogue.{AbstractQuery, Unlimited}
import com.twitter.util.{Future, FuturePool}
import net.liftweb.mongodb.record.MongoRecord

trait Db {
  def fetchOne[R](q: AbstractQuery[_, R, _, _, Unlimited, _]): Future[R]
  def fetch[R](q: AbstractQuery[_, R, _, _, _, _]): Future[List[R]]
  def save[R <: MongoRecord[R]](dbo: R): Future[R]
  def insertAll[R <: MongoRecord[R]](dbos: List[R]): Future[List[R]]
}

class MongoDb(pool: FuturePool) extends Db {
  override def fetchOne[R](q: AbstractQuery[_, R, _, _, Unlimited, _]): Future[R] = pool(q.get.get)
  override def fetch[R](q: AbstractQuery[_, R, _, _, _, _]): Future[List[R]] = pool(q.fetch)
  override def save[R <: MongoRecord[R]](dbo: R): Future[R] = pool(dbo.save)
  override def insertAll[R <: MongoRecord[R]](dbos: List[R]): Future[List[R]] = pool {
    if (!dbos.isEmpty)
      dbos.head.meta.insertAll(dbos)
    dbos
  }
}
