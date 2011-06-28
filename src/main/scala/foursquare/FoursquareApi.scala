package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import org.scala_tools.time.Imports._

case class SelfApiResponse(response: SelfApiResponseBody)
case class SelfApiResponseBody(user: UserApiResponse)
case class UserApiResponse(id: String, firstName: String, lastName: String, contact: UserContactResponse, checkins: UserCheckinsResponse)
case class UserContactResponse(phone: Option[String], email: String, twitter: Option[String], facebook: Option[String])
case class UserCheckinsResponse(count: Int)

case class CheckinsHistoryResponse(response: CheckinsHistoryResponseBody)
case class CheckinsHistoryResponseBody(checkins: CheckinsHistoryMoreResponseBody)
case class CheckinsHistoryMoreResponseBody(count: Int, items: List[CheckinDetail])
case class CheckinDetail(createdAt: Long, shout: Option[String], venue: Option[VenueDetail])
case class VenueDetail(id: String, name: Option[String], location: VenueLocation, categories: List[ParentCategory])
case class VenueLocation(address: Option[String], crossStreet: Option[String], city: String, state: String, postalCode: Option[String], country: Option[String], lat: Double, lng: Double)

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

  def checkinsUntyped(since: Option[DateTime] = None, limit: Option[Int] = Some(50)) = {
    val endpoint = "/v2/users/self/checkins"
    val params =
      Map(
        "oauth_token" -> AccessToken) ++ (since map { s =>
        "afterTimestamp" -> (s.millis / 1000).toString })
    get(endpoint, params)
  }

  def categories = {
    val endpoint = "/v2/venues/categories"
    val params =
      Map(
        "oauth_token" -> AccessToken)
    get(endpoint, params).map(_.extract[CategoriesResponse])
  }

  def checkins = checkinsUntyped(since = None).map(_.extract[CheckinsHistoryResponse])

  def allCheckins: Future[List[CheckinDetail]] = {
    val limit = 250
    for {
      selfInfo <- self
      val numCheckins = selfInfo.response.user.checkins.count
      val pages = (0 to (numCheckins / limit)).toList
      checkinPages <- Future.collect(pages.map(checkinsForPage))
    } yield checkinPages.flatten.toSet.toList
  }

  private def checkinsForPage(page: Int): Future[List[CheckinDetail]] = {
    val limit = 250
    val offset = page * 250

    val endpoint = "/v2/users/self/checkins"
    val params = 
      Map(
        "oauth_token" -> AccessToken,
        "afterTimestamp" -> "0",
        "limit" -> limit.toString,
        "offset" -> offset.toString)
    get(endpoint, params).map(_.extract[CheckinsHistoryResponse].response.checkins.items)
  }
}
