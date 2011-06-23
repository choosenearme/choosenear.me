package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}
import net.liftweb.json.{Printer, JsonAST, JsonParser}
import org.jboss.netty.util.CharsetUtil.UTF_8

case class CheckinPost(checkin: CheckinPostCheckinDetail, user: CheckinPostUserDetail)
case class CheckinPostUserDetail(id: String)
case class CheckinPostCheckinDetail(venue: VenueDetail)

class CheckinService(donorschoose: DonorsChooseApi,
                     foursquare: FoursquareApi,
                     twilio: Option[TwilioApi],
                     userDb: UserDb) extends RestApiService {
  implicit val formats = DefaultFormats

  override def post(request: RestApiRequest) = {
    val json = JsonParser.parse(request.underlying.getContent.toString(UTF_8))
    println(Printer.pretty(JsonAST.render(json)))
    println("")
    val response = json.extract[CheckinPost]

    val userId = response.user.id

    for {
      user <- userDb.fetchOne(User.where(_.foursquareId eqs userId))
      val location = response.checkin.venue.location
      val latlng = LatLng(location.lat, location.lng)
      jsonResponse <- donorschoose.near(latlng)
      val _ = println(Printer.pretty(JsonAST.render(jsonResponse)))
      val _ = println("")
      val proposals = jsonResponse.extract[DonorsChooseResponse].proposals
    } {
      val sorted = proposals.sortBy(p => LatLng(p.latitude.toDouble, p.longitude.toDouble))(LatLng.near(latlng))
      println(sorted)
      println("")
      // t.sms(user.phone.value, "Hi " + user.firstName + ", the nearest DonorsChoose school is " + proposal.schoolName)
    }

    Future.value(new RestApiResponse(JObject(List())))
  }
}
