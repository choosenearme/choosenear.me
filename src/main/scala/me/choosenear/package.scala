package me

import com.foursquare.rogue.Rogue
import com.twitter.util.Future

package object choosenear extends Rogue {
  implicit def RichFuture[A](future: Future[A]): RichFuture[A] = new RichFuture(future)
}
