package choosenearme

import com.twitter.util.Future
import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.json.JValue

case class DonorsChooseResponse(proposals: List[DonorsChooseProposal])
case class DonorsChooseProposal(id: String, schoolName: String, latitude: String, longitude: String, subject: DonorsChooseSubject)
case class DonorsChooseSubject(name: String)

class DonorsChooseApi(config: DonorsChooseConfig) extends JsonApiClient("api.donorschoose.org") {
  implicit val formats = new net.liftweb.json.DefaultFormats {
    override protected def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  def projectInfo(projectId: String): Future[JValue] = {
    val endpoint = "/common/json_feed.html"
    val params =
      Map(
        "APIKey" -> config.key,
        "id" -> projectId)
    get(endpoint, params)
  }

  def nearKeyword(geo: LatLong, keyword: String) = {
    val endpoint = "/common/json_feed.html"
    val params =
      Map(
        "APIKey" -> config.key,
        "centerLat" -> geo.lat.toString,
        "centerLng" -> geo.long.toString,
        "keywords" -> keyword)
    get(endpoint, params)
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
    val pages = (0 until 5).toList
    for {
      checkinPages <- Future.collect(pages.map(withinPage(center, radiusInMeters)))
    } yield checkinPages.map(_ \ "proposals").reduceLeft(_ ++ _)
  }

  def withinPage(center: LatLong, radiusInMeters: Double)(page: Int) = {
    val maxPerPage = 50
    val index = page*maxPerPage
    val radius = radiusInMeters * 0.000621371192 // in miles
    val endpoint = "/common/json_feed.html"
    val params = 
      Map(
        "APIKey" -> config.key,
        "centerLat" -> center.lat.toString,
        "centerLng" -> center.long.toString,
        "radius" -> radius.toString,
        "max" -> maxPerPage.toString,
        "index" -> index.toString)
    get(endpoint, params)
  }
}
