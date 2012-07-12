package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JBool}
import net.liftweb.json.{Printer, JsonAST, JsonParser}
import org.bson.types.ObjectId
import org.jboss.netty.handler.codec.http.QueryStringDecoder
import org.jboss.netty.util.CharsetUtil.UTF_8

case class CheckinPost(checkin: CheckinDetail, user: CheckinPostUserDetail)
case class CheckinPostUserDetail(id: String)
// case class CheckinPostCheckinDetail(id: String, venue: Option[VenueDetail])

class CheckinService(
    donorschoose: DonorsChooseApi,
    foursquare: FoursquareApi,
    twilio: Option[TwilioApi],
    db: Db
) extends RestApiService {
  implicit val formats = DefaultFormats

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")
    val id = request.params.required[ObjectId]("checkinId")

    for {
      userFoursquareId <- db.fetchOne(User.where(_.secret eqs secret).select(_.foursquareId))
      checkin <- db.fetchOne(Checkin.where(_.userId eqs userFoursquareId).and(_._id eqs id))
      val latlng = checkin.latlng.value
      val matchingSubjects = CategoryUtil.subjectsForCategories(checkin.categories.value)
      val responses = donorschoose.near(latlng) +: matchingSubjects.map(subject => donorschoose.nearKeyword(latlng, subject))
      jsonResponses <- Future.collect(responses)
    } yield {
      val proposals = jsonResponses.flatMap(_.extract[DonorsChooseResponse].proposals)
      val matchingProposals = proposals.filter(proposal => matchingSubjects.contains(proposal.subject.name)).groupBy(_.id)

      val proposalsJson = JObject(List(JField("proposals", jsonResponses.map(_ \ "proposals").reduceLeft(_ ++ _))))
      val transformedJson = 
        proposalsJson transform {
          case JObject(List(JField("id", JString(id)), fields @ _*)) if matchingProposals.contains(id) =>
            JObject(List(JField("id", JString(id)), JField("matchesCheckin", JBool(true))) ++ fields)
        }

      new RestApiResponse(JObject(List(JField("proposals", transformedJson))))
    }
  }

  override def post(request: RestApiRequest) = {
    val content = request.underlying.getContent.toString(UTF_8)
    val decoder = new QueryStringDecoder("/?" + content)
    val params = RestApiParameters.fromDecoder(decoder)
    val checkinContent = params.required[String]("checkin")
    val json = JsonParser.parse(checkinContent)
    val checkinDetail = json.extract[CheckinDetail]

    for {
      userId <- checkinDetail.user.map(_.id)
      user <- db.fetchOne(User.where(_.foursquareId eqs userId))
      checkin <- Checkin.fromCheckinDetail(user)(checkinDetail)
    } {
      db.save(checkin)
      val subjects = CategoryUtil.subjectsForCategories(checkin.categories.value)
      for {
        venue <- checkinDetail.venue
        val location = venue.location
        val latlng = LatLong(location.lat, location.lng)
        jsonResponses <- Future.collect(subjects.map(subject => donorschoose.nearKeyword(latlng, subject)))
      } {
        val proposals = jsonResponses.flatMap(_.extract[DonorsChooseResponse].proposals)
        val matchingProposals = proposals.filter(p => subjects.contains(p.subject.name))
        val ord = LatLongUtil.near(latlng)
        val sortedProposals = proposals.sortBy(p => LatLong(p.latitude.toDouble, p.longitude.toDouble))(ord)
        sortedProposals.headOption.foreach(proposal => {
          val authedFoursquare = foursquare.authenticateUser(user)
          val text = proposal.title + " is nearby at " + proposal.schoolName + "! Tap for more information."
          val url = "https://choosenear.me/proposals/#!" + proposal.id

          authedFoursquare.reply(
            checkinId = checkin.id.toString,
            text = text,
            url = url)
        })
      }
    }

    Future.value(new RestApiResponse(JObject(List())))
  }
}
