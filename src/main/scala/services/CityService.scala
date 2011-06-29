package choosenearme

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JInt, JArray, JDouble, JNothing}

class CityService(db: Db, foursquare: FoursquareApi, donorschoose: DonorsChooseApi) extends RestApiService {
  implicit val formats = net.liftweb.json.DefaultFormats

  private def jsonForCheckin(proposals: List[DonorsChooseProposal])(checkin: Checkin): JObject = {
    val checkinCategories = checkin.categories.value
    val matchingSubjects = checkinCategories.flatMap(category => CategoryUtil.matchingMap.get(category)).flatten
    val matchingProposals = proposals.filter(proposal => matchingSubjects.contains(proposal.subject.name))
    val matchingProposalsJson =
      if (matchingProposals.isEmpty) JNothing
      else {
        println("WOOT! Found a match!")
        println(checkin)
        println(matchingProposals)
        println("")
        JArray(matchingProposals.map(proposal => JString(proposal.id)))
      }

    JObject(List(
      JField("id", JString(checkin.id.toString)),
      JField("venuename", JString(checkin.venuename.value)),
      JField("lat", JDouble(checkin.latlng.lat)),
      JField("lng", JDouble(checkin.latlng.long)),
      JField("categories", JArray(checkin.categories.value.map(JString.apply))),
      JField("matchingProposals", matchingProposalsJson)))
  }

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")
    val latlng = request.params.required[LatLong]("latlng")

    for {
      user <- db.fetchOne(User.where(_.secret eqs secret))
      val checkinsQuery =
        (Checkin
          .where(_.latlng near (latlng.lat, latlng.long, City.CityRadiusInDegrees))
          .and(_.userId eqs user.foursquareId.value)
          .orderDesc(_._id)
          .limit(10000))
      // Start both queries in parallel
      val checkinsF = db.fetch(checkinsQuery)
      val proposalsF = donorschoose.within(latlng, City.CityRadiusInMeters)
      // Wait for both queries to complete
      (checkins, proposalsJson) <- (checkinsF join proposalsF)
    } yield {
      val proposals = proposalsJson.extract[DonorsChooseResponse].proposals
      val checkinFields = checkins.map(jsonForCheckin(proposals))
      val response =
        JObject(List(
          JField("proposals", proposalsJson \ "proposals"),
          JField("checkins", JArray(checkinFields))))
      new RestApiResponse(JObject(List(JField("response", response))))
    }
  }
}
