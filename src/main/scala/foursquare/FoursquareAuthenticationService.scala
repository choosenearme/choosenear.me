package choosenearme

import com.twitter.finagle.Service
import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest, DefaultHttpResponse, QueryStringEncoder}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, FOUND}
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class FoursquareAuthenticationService(authApi: FoursquareAuthenticationApi, fsqApi: FoursquareApi, userDb: UserDb) extends Service[HttpRequest, HttpResponse] {
  def redirectTo(uri: String): HttpResponse = {
    val response = new DefaultHttpResponse(HTTP_1_1, FOUND)
    response.addHeader("Location", uri)
    response
  }

  override def apply(_request: HttpRequest) = {
    val request = RestApiRequest.fromHttpRequest(_request)

    request.path match {
      case "auth" :: Nil =>
        val uri = {
          val encoder = new QueryStringEncoder("https://foursquare.com/oauth2/authenticate")
          encoder.addParam("client_id", authApi.config.key)
          encoder.addParam("response_type", "code")
          encoder.addParam("redirect_uri", authApi.config.callback)
          encoder.toString
        }
        Future.value(redirectTo(uri))
      case "auth" :: "callback" :: Nil =>
        val code = request.params.required[String]("code")
        for {
          accessToken <- authApi.auth(code)
          userInfo <- fsqApi.authenticate(accessToken).self
          user <- (userDb.fetchOne(User.where(_.foursquareId eqs userInfo.response.user.id))
                         .rescue {
                           case ex: java.util.NoSuchElementException =>
                             userDb.save(User.createRecord
                                  .foursquareId(userInfo.response.user.id)
                                  .foursquareToken(accessToken)
                                  .firstName(userInfo.response.user.firstName)
                                  .lastName(userInfo.response.user.lastName)
                                  .email(userInfo.response.user.contact.email)
                                  .phone(userInfo.response.user.contact.phone))
                         })
        } yield {
          redirectTo("/?secret=" + user.secret.value)
        }
      case _ =>
        error("TODO")
    }
  }
}
