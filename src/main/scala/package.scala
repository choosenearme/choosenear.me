import com.foursquare.rogue.Rogue
import com.twitter.util.Future

package object choosenearme extends Rogue {
  implicit def RichFuture[A](future: Future[A]): RichFuture[A] = new RichFuture(future)
  implicit def RichList[A](xs: List[A]): RichList[A] = new RichList(xs)
}
