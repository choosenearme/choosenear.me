package me.choosenear

import net.liftweb.json.DefaultFormats

case class SelfApiResponse(response: SelfApiResponseBody)
case class SelfApiResponseBody(user: UserApiResponse)
case class UserApiResponse(id: String, firstName: String, lastName: String, contact: UserContactResponse)
case class UserContactResponse(phone: String, email: String, twitter: String, facebook: String)

// case class CheckinsHistoryRespons(response: CheckinsHistoryResponseBody)
// case class CheckinsHistoryResponseBody(checkins: CheckinsHistoryMoreResponseBody)
// case class CheckinsHistoryMoreResponseBody(count: Int, items: List[CheckinDetail])
// case class CheckinDetail(venue: VenueDetail, createdAt: Long)

class FoursquareApi(val ClientId: String, ClientSecret: String) {
  def authenticate(accessToken: String) = new AuthenticatedFoursquareApi(ClientId, ClientSecret, accessToken)
  def authenticateUser(user: User) = new AuthenticatedFoursquareApi(ClientId, ClientSecret, user.foursquareToken.value)
}

class AuthenticatedFoursquareApi(val ClientId: String, ClientSecret: String, AccessToken: String) extends JsonApiClient("api.foursquare.com", 443) {
  override def clientBuilder = super.clientBuilder.tls

  implicit val formats = DefaultFormats

  def self = {
    val endpoint = "/v2/users/self"
    val params =
      Map(
        "oauth_token" -> AccessToken)
    call(endpoint, params).map(_.extract[SelfApiResponse])
  }
}
