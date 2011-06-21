package choosenearme

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JObject, JField, JString}

class FoursquareAuthenticationApi(val config: FoursquareConfig) extends JsonApiClient("foursquare.com", 443) {
  override def clientBuilder = super.clientBuilder.tls

  def auth(code: String): Future[String] = {
    val endpoint = "/oauth2/access_token"
    val params =
      Map(
        "client_id" -> config.key,
        "client_secret" -> config.secret,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> config.callback,
        "code" -> code)
    get(endpoint, params) collect {
      case JObject(List(JField("access_token", JString(accessToken)))) =>
        accessToken
    }
  }
}
