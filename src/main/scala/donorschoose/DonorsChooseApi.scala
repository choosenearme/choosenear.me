package choosenearme

import java.text.SimpleDateFormat
import java.util.Date

case class DonorsChooseResponse(proposals: List[DonorsChooseProposal])
case class DonorsChooseProposal(schoolName: String, latitude: String, longitude: String)

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
}
