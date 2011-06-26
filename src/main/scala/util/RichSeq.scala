package choosenearme

class RichSeq[A](xs: Seq[A]) {
  def minBy[B](f: A => B)(implicit ord: Ordering[B]): A = {
    xs.reduceLeft((x, y) => if (ord.lteq(f(x), f(y))) x else y)
  }
}
