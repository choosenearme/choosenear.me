package me.choosenear

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JObject, JField, JString}

class FoursquareAuthenticationApi(val RedirectUri: String,
                                  val ClientId: String,
                                  ClientSecret: String) extends JsonApiClient("foursquare.com") {
  def auth(code: String): Future[String] = {
    val endpoint = "/oauth2/access_token"
    val params =
      Map(
        "client_id" -> ClientId,
        "client_secret" -> ClientSecret,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> RedirectUri,
        "code" -> code)
    call(endpoint, params) collect {
      case JObject(List(JField("access_token", JString(accessToken)))) =>
        accessToken
    }
  }
}
