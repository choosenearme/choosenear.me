package choosenearme

import choosenearme.Parser.DoubleParser
import scala.collection.mutable.ArrayBuffer

object LatLongUtil {
  implicit object LatLongParser extends Parser[LatLong] {
    override def apply(x: String): Option[LatLong] = {
      x.split(",") match {
        case Array(DoubleParser(lat), DoubleParser(lng)) => Some(LatLong(lat, lng))
        case _ => None
      }
    }
  }

  def distance(x: LatLong, y: LatLong): Double = {
    val latDelta = y.lat - x.lat
    val lngDelta = y.long - x.long
    
    math.sqrt(latDelta*latDelta + lngDelta*lngDelta)
  }

  def near(reference: LatLong): Ordering[LatLong] = new Ordering[LatLong] {
    override def compare(x: LatLong, y: LatLong): Int = {
      val distX = distance(reference, x)
      val distY = distance(reference, y)
      distX compareTo distY
    }
  }

  def centroid(latlngs: List[LatLong]): LatLong = {
    val sum =
      (latlngs.foldLeft(LatLong(0.0, 0.0)) { case (LatLong(x1, y1), LatLong(x2, y2)) =>
        LatLong(x1 + x2, y1 + y2)
      })
    val size = latlngs.size
    val centroid = LatLong(sum.lat / size, sum.long / size)
    centroid
  }

  def cluster(latlngs: List[LatLong], maxRadiusInMeters: Double): List[LatLong] = {
    case class Cluster(var centroid: LatLong, val points: ArrayBuffer[LatLong])

    val maxRadius = maxRadiusInMeters / 111120.0 // in degrees
    val clusters = ArrayBuffer[Cluster]()

    for (latlng <- latlngs) {
      if (clusters.isEmpty)
        clusters += Cluster(latlng, ArrayBuffer())

      val nearestCluster = clusters.minBy(_.centroid)(LatLongUtil.near(latlng))

      if (LatLongUtil.distance(latlng, nearestCluster.centroid) < maxRadius) {
        val newCentroid = {
          val size = nearestCluster.points.size
          val newLat = (nearestCluster.centroid.lat * size + latlng.lat) / (size + 1)
          val newLng = (nearestCluster.centroid.long * size + latlng.long) / (size + 1)
          LatLong(newLat, newLng)
        }
        nearestCluster.points += latlng
        nearestCluster.centroid = newCentroid
      } else {
        clusters += Cluster(latlng, ArrayBuffer(latlng))
      }
    }

    clusters.map(_.centroid).toList
  }
}
