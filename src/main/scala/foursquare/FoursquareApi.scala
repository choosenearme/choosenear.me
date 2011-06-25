package choosenearme

import net.liftweb.json.DefaultFormats
import org.scala_tools.time.Imports._

case class SelfApiResponse(response: SelfApiResponseBody)
case class SelfApiResponseBody(user: UserApiResponse)
case class UserApiResponse(id: String, firstName: String, lastName: String, contact: UserContactResponse)
case class UserContactResponse(phone: Option[String], email: String, twitter: Option[String], facebook: Option[String])

case class CheckinsHistoryResponse(response: CheckinsHistoryResponseBody)
case class CheckinsHistoryResponseBody(checkins: CheckinsHistoryMoreResponseBody)
case class CheckinsHistoryMoreResponseBody(count: Int, items: List[CheckinDetail])
case class CheckinDetail(createdAt: Long, venue: VenueDetail)
case class VenueDetail(name: String, shout: Option[String], location: VenueLocation)
case class VenueLocation(address: Option[String], crossStreet: Option[String], city: String, state: String, postalCode: Option[String], country: Option[String], lat: Double, lng: Double)

class FoursquareApi {
  def authenticate(accessToken: String) = new AuthenticatedFoursquareApi(accessToken)
  def authenticateUser(user: User) = new AuthenticatedFoursquareApi(user.foursquareToken.value)
}

class AuthenticatedFoursquareApi(AccessToken: String) extends JsonApiClient("api.foursquare.com", 443) {
  override def clientBuilder = super.clientBuilder.tls

  implicit val formats = DefaultFormats

  def self = {
    val endpoint = "/v2/users/self"
    val params =
      Map(
        "oauth_token" -> AccessToken)
    get(endpoint, params).map(_.extract[SelfApiResponse])
  }

  def checkinsUntyped(since: Option[DateTime] = None, limit: Option[Int] = Some(50)) = {
    val endpoint = "/v2/users/self/checkins"
    val params =
      Map(
        "oauth_token" -> AccessToken) ++ (since map { s =>
        "afterTimestamp" -> (s.millis / 1000).toString })
    get(endpoint, params)
  }

  def checkins = checkinsUntyped(since = None).map(_.extract[CheckinsHistoryResponse])
}
