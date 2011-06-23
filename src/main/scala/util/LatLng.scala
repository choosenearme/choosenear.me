package choosenearme

import Parser.DoubleParser

case class LatLng(lat: Double, lng: Double)

object LatLng {
  implicit object LatLngParser extends Parser[LatLng] {
    override def apply(x: String): Option[LatLng] = {
      x.split(",") match {
        case Array(DoubleParser(lat), DoubleParser(lng)) => Some(LatLng(lat, lng))
        case _ => None
      }
    }
  }

  def distance(x: LatLng, y: LatLng): Double = {
    val latDelta = y.lat - x.lat
    val lngDelta = y.lng - x.lng
    
    math.sqrt(latDelta*latDelta + lngDelta*lngDelta)
  }

  def near(reference: LatLng): Ordering[LatLng] = new Ordering[LatLng] {
    override def compare(x: LatLng, y: LatLng): Int = {
      val distX = distance(reference, x)
      val distY = distance(reference, y)
      distX compareTo distY
    }
  }
}
