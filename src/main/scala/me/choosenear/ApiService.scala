package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpResponse, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class ApiService extends Service[RichHttpRequest, HttpResponse] {
  override def apply(request: RichHttpRequest) = {
    request.path match {
      case "location" :: Nil =>
      case _ =>
    }
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    response.setContent(copiedBuffer("hello world", UTF_8))
    Future.value(response)
  }
}
