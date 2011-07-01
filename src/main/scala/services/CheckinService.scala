package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JBool}
import net.liftweb.json.{Printer, JsonAST, JsonParser}
import org.bson.types.ObjectId
import org.jboss.netty.util.CharsetUtil.UTF_8

case class CheckinPost(checkin: CheckinDetail, user: CheckinPostUserDetail)
case class CheckinPostUserDetail(id: String)
// case class CheckinPostCheckinDetail(id: String, venue: Option[VenueDetail])

class CheckinService(donorschoose: DonorsChooseApi,
                     foursquare: FoursquareApi,
                     twilio: Option[TwilioApi],
                     db: Db) extends RestApiService {
  implicit val formats = DefaultFormats

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")
    val id = request.params.required[ObjectId]("checkinId")

    for {
      user <- db.fetchOne(User.where(_.secret eqs secret))
      checkin <- db.fetchOne(Checkin.where(_.userId eqs user.foursquareId.value).and(_._id eqs id))
      proposalsJson <- donorschoose.near(checkin.latlng.value)
    } yield {
      val proposals = proposalsJson.extract[DonorsChooseResponse].proposals
      val checkinCategories = checkin.categories.value
      val matchingSubjects = checkinCategories.flatMap(category => CategoryUtil.matchingMap.get(category)).flatten
      val matchingProposals = proposals.filter(proposal => matchingSubjects.contains(proposal.subject.name)).groupBy(_.id)
      if (!matchingProposals.isEmpty) {
        println("Found matches!")
        println(matchingProposals)
      }

      val transformedJson = 
        proposalsJson transform {
          case JObject(List(JField("id", JString(id)), fields @ _*)) if matchingProposals.contains(id) =>
            JObject(List(JField("id", JString(id)), JField("matchesCheckin", JBool(true))) ++ fields)
        }

      new RestApiResponse(JObject(List(JField("proposals", transformedJson))))
    }
  }

  override def post(request: RestApiRequest) = {
    val json = JsonParser.parse(request.underlying.getContent.toString(UTF_8))
    val response = json.extract[CheckinPost]
    val userId = response.user.id

    for {
      user <- db.fetchOne(User.where(_.foursquareId eqs userId))
      record <- Checkin.fromCheckinDetail(user)(response.checkin)
      _ <- db.save(record)
      // val location = response.checkin.venue.location
      // val latlng = LatLong(location.lat, location.lng)
      // jsonResponse <- donorschoose.near(latlng)
      // val _ = println(Printer.pretty(JsonAST.render(jsonResponse)))
      // val _ = println("")
      // val proposals = jsonResponse.extract[DonorsChooseResponse].proposals
    } {
      // val sorted = proposals.sortBy(p => LatLong(p.latitude.toDouble, p.longitude.toDouble))(LatLongUtil.near(latlng))
      // println(sorted)
      // println("")
      // t.sms(user.phone.value, "Hi " + user.firstName + ", the nearest DonorsChoose school is " + proposal.schoolName)
    }

    Future.value(new RestApiResponse(JObject(List())))
  }
}
