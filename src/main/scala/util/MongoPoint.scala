package choosenearme

import com.mongodb.{BasicDBList, DBObject}
import net.liftweb.common.{Failure, Empty, Full, Box}
import net.liftweb.http.js.JE.{JsObj, Num}
import net.liftweb.json.JsonAST._
import net.liftweb.mongodb.record.field.MongoFieldFlavor
import net.liftweb.record._
import net.liftweb.util.Helpers
import xml.{Text, NodeSeq}

class MongoPoint[OwnerType <: Record[OwnerType]](rec: OwnerType) extends Field[LatLong, OwnerType] with MandatoryTypedField[LatLong] with MongoFieldFlavor[LatLong] {
  override type MyType = LatLong

  def owner = rec

  override def toForm = error("Unimplemented")
  override def asJValue = error("Unimplemented")
  override def setFromJValue(jvalue: JValue) = error("Unimplemented")
  override def defaultValue = error("Unimplemented")
  override def setFromString(in: String) = error("Unimplemented")

  override def defaultValueBox = Empty

  def lat = value.lat
  def long = value.long

  def asDBObject: DBObject = {
    val dbl = new BasicDBList
    dbl.add(value.lat.asInstanceOf[Object])
    dbl.add(value.long.asInstanceOf[Object])
    dbl
  }

  private def toDouble(o: Any): Double = o match {
    case d: Double => d
    case d: Int => d.toDouble
    case _ => throw new IllegalArgumentException("Expecting a double but got: " + o)
  }
  
  def setFromDBObject(dbo: DBObject): Box[LatLong] = {
    val lat = toDouble(dbo.get("0"))
    val long = toDouble(dbo.get("1"))
    setBox(Full(LatLong(lat,long)))
  }

  def setFromAny(in: Any): Box[LatLong] = in match {
    case dbo: DBObject => setFromDBObject(dbo)
    case latlong: LatLong => setBox(Full(latlong))
    case (lat: Double) :: (long: Double) :: _ =>  setBox(Full(LatLong(lat,long)))
    case null|None|Empty     => setBox(defaultValueBox)
    case (failure: Failure)  => setBox(failure)
    case _ => setBox(defaultValueBox)
  }
}
