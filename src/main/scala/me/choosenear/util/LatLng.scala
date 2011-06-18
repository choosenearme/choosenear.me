package me.choosenear

import Parser.DoubleParser

case class LatLng(lat: Double, long: Double)

object LatLng {
  implicit object LatLngParser extends Parser[LatLng] {
    override def apply(x: String): Option[LatLng] = {
      x.split(",") match {
        case Array(DoubleParser(lat), DoubleParser(lng)) => Some(LatLng(lat, lng))
        case _ => None
      }
    }
  }
}
