package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class RestApiFilter extends HttpFilter[RestApiRequest, HttpResponse] {
  override def apply(request: HttpRequest, service: Service[RestApiRequest, HttpResponse]): Future[HttpResponse] = {
    service(new RestApiRequest(request)) handle {
      case RestApiException(msg, status) =>
        val response = new DefaultHttpResponse(HTTP_1_1, status)
        response.setContent(copiedBuffer(msg, UTF_8))
        response
    }
  }
}
