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

  def centroid(latlngs: List[LatLng]): LatLng = {
    val sum =
      (latlngs.foldLeft(LatLng(0.0, 0.0)) { case (LatLng(x1, y1), LatLng(x2, y2)) =>
        LatLng(x1 + x2, y1 + y2)
      })
    val size = latlngs.size
    val centroid = LatLng(sum.lat / size, sum.lng / size)
    centroid
  }

  def cluster(latlngs: List[LatLng], maxRadiusInMeters: Double): List[LatLng] = {
    case class Cluster(var centroid: LatLng, var points: List[LatLng])

    val maxRadius = maxRadiusInMeters / 111120.0 // in degrees
    var clusters: List[Cluster] = Nil

    for (latlng <- latlngs) {
      if (clusters.isEmpty)
        clusters ::= Cluster(latlng, Nil)

      val nearestCluster = clusters.minBy(_.centroid)(LatLng.near(latlng))

      if (LatLng.distance(latlng, nearestCluster.centroid) < maxRadius) {
        nearestCluster.points ::= latlng
        nearestCluster.centroid = centroid(nearestCluster.points)
      } else {
        clusters ::= Cluster(latlng, latlng :: Nil)
      }
    }

    clusters.map(_.centroid)
  }
}
