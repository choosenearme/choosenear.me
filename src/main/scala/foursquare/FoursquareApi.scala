package choosenearme

import net.liftweb.json.DefaultFormats

case class SelfApiResponse(response: SelfApiResponseBody)
case class SelfApiResponseBody(user: UserApiResponse)
case class UserApiResponse(id: String, firstName: String, lastName: String, contact: UserContactResponse)
case class UserContactResponse(phone: Option[String], email: String, twitter: Option[String], facebook: Option[String])

case class CheckinsHistoryResponse(response: CheckinsHistoryResponseBody)
case class CheckinsHistoryResponseBody(checkins: CheckinsHistoryMoreResponseBody)
case class CheckinsHistoryMoreResponseBody(count: Int, items: List[CheckinDetail])
case class CheckinDetail(createdAt: Long, venue: VenueDetail)
case class VenueDetail(name: String, shout: Option[String], location: VenueLocation, categories: List[ParentCategory])
case class VenueLocation(address: String, crossStreet: Option[String], city: String, state: String, postalCode: Option[String], country: Option[String], lat: Double, lng: Double)

case class CategoriesResponse(response: CategoriesResponseBody)
case class CategoriesResponseBody(categories: List[ParentCategory])
case class ParentCategory(name: String, pluralName: String, icon: String, categories: Option[List[SubCategory]])
case class SubCategory(name: String, pluralName: String, icon: String, categories: Option[List[SubSubCategory]])
case class SubSubCategory(name: String, pluralName: String, icon: String)

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

  def checkinsUntyped = {
    val endpoint = "/v2/users/self/checkins"
    val params =
      Map(
        "oauth_token" -> AccessToken)
      get(endpoint, params)
  }

  def categories = {
	val endpoint = "/v2/venues/categories"
	val params =
	  Map(
	  	"oauth_token" -> AccessToken)
	  get(endpoint, params).map(_.extract[CategoriesResponse])
  }

  def checkins = checkinsUntyped.map(_.extract[CheckinsHistoryResponse])
}
