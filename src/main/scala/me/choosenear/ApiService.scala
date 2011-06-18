package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpResponse, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class ApiService(donorsChoose: DonorsChooseApi) extends Service[RestApiRequest, HttpResponse] {
  override def apply(request: RestApiRequest) = {
    request.path match {
      case "location" :: Nil =>
        val latlng = request.params.required[LatLng]("latlng")
        val proposals = donorsChoose.near(latlng)
      case _ =>
    }
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    response.setContent(copiedBuffer("hello world", UTF_8))
    Future.value(response)
  }
}
