package me

import com.twitter.util.Future

package object choosenear {
  implicit def RichFuture[A](future: Future[A]): RichFuture[A] = new RichFuture(future)
}