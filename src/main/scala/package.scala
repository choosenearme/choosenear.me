import com.foursquare.rogue.Rogue
import com.twitter.util.Future

package object choosenearme extends Rogue {
  type LatLong = com.foursquare.rogue.LatLong
  val LatLong = com.foursquare.rogue.LatLong

  implicit val LatLongParser = LatLongUtil.LatLongParser
  implicit def RichFuture[A](future: Future[A]): RichFuture[A] = new RichFuture(future)
  implicit def RichSeq[A](xs: Seq[A]): RichSeq[A] = new RichSeq(xs)
  implicit def RichMultiMap[A, B](m: Map[A, List[B]]): RichMultiMap[A, B] = new RichMultiMap(m)
}
