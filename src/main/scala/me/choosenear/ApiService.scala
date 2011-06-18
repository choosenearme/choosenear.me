package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{HttpResponse, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.OK
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class ApiService(donorsChoose: DonorsChooseApi) extends Service[RestApiRequest, RestApiResponse] {
  implicit val formats = DefaultFormats

  override def apply(request: RestApiRequest) = {
    request.path match {
      case "location" :: Nil =>
        val latlng = request.params.required[LatLng]("latlng")
        val resultFuture = donorsChoose.near(latlng)
        resultFuture map { result =>
          new RestApiResponse(JObject(List(JField("proposals", decompose(result)))))
        }
      case _ =>
        Future.value(new RestApiResponse(JObject(List(JField("hello", JString("world"))))))
    }
  }
}
