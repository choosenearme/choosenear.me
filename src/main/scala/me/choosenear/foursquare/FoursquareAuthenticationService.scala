package me.choosenear

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

class FoursquareAuthenticationService(authApi: FoursquareAuthenticationApi) extends Service[HttpRequest, HttpResponse] {
  override def apply(_request: HttpRequest) = {
    val request = new RestApiRequest(_request)

    request.path match {
      case "auth" :: Nil =>
        val uri = {
          val encoder = new QueryStringEncoder("https://foursquare.com/oauth2/authenticate")
          encoder.addParam("client_id", authApi.ClientId)
          encoder.addParam("response_type", "code")
          encoder.addParam("redirect_uri", authApi.RedirectUri)
          encoder.toString
        }

        val response = new DefaultHttpResponse(HTTP_1_1, FOUND)
        response.addHeader("Location", uri)
        Future.value(response)
      case "auth" :: "callback" :: Nil =>
        val code = request.params.required[String]("code")
        authApi.auth(code) map { accessToken =>
          
        }
        error("TODO")
      case _ =>
        error("TODO")
    }
  }
}
