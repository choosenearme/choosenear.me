package choosenearme

class RichList[A](xs: List[A]) {
  def minBy[B](f: A => B)(implicit ord: Ordering[B]): A = {
    xs.reduceLeft((x, y) => if (ord.lteq(f(x), f(y))) x else y)
  }
}
