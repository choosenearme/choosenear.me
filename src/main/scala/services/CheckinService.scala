package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}
import net.liftweb.json.JsonParser
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
    val response = json.extract[CheckinPost]

    val userId = response.user.id
    if (userId == "646") {
      for {
        user <- userDb.fetchOne(User.where(_.foursquareId eqs userId))
        val location = response.checkin.venue.location
        val latlng = LatLng(location.lat, location.lng)
        jsonResponse <- donorschoose.near(latlng)
        proposal <- jsonResponse.extract[DonorsChooseResponse].proposals.headOption
        t <- twilio
      } {
        t.sms(user.phone.value, "Hi " + user.firstName + ", the nearest DonorsChoose school is " + proposal.schoolName)
      }
    }

    Future.value(new RestApiResponse(JObject(List())))
  }
}
