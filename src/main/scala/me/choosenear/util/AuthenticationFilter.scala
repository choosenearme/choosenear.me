package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest, DefaultHttpResponse, QueryStringDecoder}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1

class AuthenticationFilter(authService: Service[HttpRequest, HttpResponse]) extends HttpFilter[HttpRequest, HttpResponse] {
  override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
    val decoder = new QueryStringDecoder(request.getUri)
    if (decoder.getPath.startsWith("/auth"))
      authService(request) handle { case _: MissingParameterException =>
        val response = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST)
        response
      }
    else
      service(request)
  }
}
