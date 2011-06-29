package choosenearme

class RichMultiMap[A, B](m: Map[A, List[B]]) {
  def invert: Map[B, List[A]] = {
    val invertedList = 
      for {
        (k, vs) <- m.toList
        v <- vs
      } yield (v, k)
    invertedList.groupBy(_._1).mapValues(_.map(_._2))
  }
}
