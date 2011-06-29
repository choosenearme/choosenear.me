package choosenearme

import java.text.SimpleDateFormat
import java.util.Date

case class DonorsChooseResponse(proposals: List[DonorsChooseProposal])
case class DonorsChooseProposal(id: String, schoolName: String, latitude: String, longitude: String, subject: DonorsChooseSubject)
case class DonorsChooseSubject(name: String)

class DonorsChooseApi(config: DonorsChooseConfig) extends JsonApiClient("api.donorschoose.org") {
  implicit val formats = new net.liftweb.json.DefaultFormats {
    override protected def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  def near(geo: LatLong) = {
    val endpoint = "/common/json_feed.html"
    val params =
      Map(
        "APIKey" -> config.key,
        "centerLat" -> geo.lat.toString,
        "centerLng" -> geo.long.toString)
    get(endpoint, params)
  }

  def within(center: LatLong, radiusInMeters: Double) = {
    val max = 50
    val index = 0
    val radius = radiusInMeters * 0.000621371192 // in miles
    val endpoint = "/common/json_feed.html"
    val params = 
      Map(
        "APIKey" -> config.key,
        "centerLat" -> center.lat.toString,
        "centerLng" -> center.long.toString,
        "radius" -> radius.toString,
        "max" -> max.toString,
        "index" -> index.toString)
    get(endpoint, params)
  }
}
