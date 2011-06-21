package choosenearme

import com.twitter.util.Future

class RichFuture[A](future: Future[A]) {
  def collect[B](pf: PartialFunction[A, B]): Future[B] =
    future.filter(pf.isDefinedAt).map(pf)
}
