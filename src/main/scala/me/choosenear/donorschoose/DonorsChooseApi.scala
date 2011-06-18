package me.choosenear

import java.text.SimpleDateFormat
import java.util.Date

case class DonorsChooseResult(totalProposals: String, proposals: List[DonorsChooseProposal])
case class DonorsChooseProposal(id: String,
                                proposalURL: String,
                                title: String,
                                shortDescription: String,
                                fulfillmentTrailer: String,
                                percentFunded: String,
                                costToComplete: String,
                                totalPrice: String,
                                teacherName: String,
                                latitude: String,
                                longitude: String,
                                expirationDate: Date)

class DonorsChooseApi(ApiKey: String = "DONORSCHOOSE") extends JsonApiClient("api.donorschoose.org") {
  implicit val formats = new net.liftweb.json.DefaultFormats {
    override protected def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  def near(geo: LatLng) = {
    val endpoint = "/common/json_feed.html"
    val params =
      Map(
        "APIKey" -> ApiKey,
        "centerLat" -> geo.lat.toString,
        "centerLng" -> geo.long.toString)
    call(endpoint, params).map(_.extract[DonorsChooseResult])
  }
}
