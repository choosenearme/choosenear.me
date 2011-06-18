package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import net.liftweb.json.{JsonAST, Printer}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.util.CharsetUtil.UTF_8

class RestApiFilter extends HttpFilter[RestApiRequest, RestApiResponse] {
  override def apply(request: HttpRequest, service: Service[RestApiRequest, RestApiResponse]): Future[HttpResponse] = {
    service(new RestApiRequest(request)) map { apiResponse =>
      val response = new DefaultHttpResponse(HTTP_1_1, OK)
      response.setContent(copiedBuffer(Printer.pretty(JsonAST.render(apiResponse.json)) + "\n", UTF_8))
      response
    } handle { case RestApiException(msg, status) =>
      val response = new DefaultHttpResponse(HTTP_1_1, status)
      response.setContent(copiedBuffer(msg, UTF_8))
      response
    }
  }
}
