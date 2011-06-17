package me.choosenear

import com.twitter.finagle.builder.{ClientBuilder, Http}
import com.twitter.util.Future
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JValue
import org.jboss.netty.handler.codec.http.{QueryStringEncoder, DefaultHttpRequest}
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class RestJsonApi(host: String, port: Int = 80) {
  val client =
    (ClientBuilder()
      .codec(Http)
      .hosts(host + ":" + port)
      .hostConnectionLimit(10)
      .build())

  def call(endpoint: String, params: Map[String, String]): Future[JValue] = {
    val uri = {
      val encoder = new QueryStringEncoder(endpoint)
      for ((k, v) <- params)
        encoder.addParam(k, v)
      encoder.toString
    }
    val request = new DefaultHttpRequest(HTTP_1_1, GET, uri)
    request.addHeader("Accept", "*/*")
    request.addHeader("User-Agent", "choosenear.me-api/1.0")
    request.addHeader("Host", host)
    client(request) map { response =>
      val content = response.getContent.toString(UTF_8)
      JsonParser.parse(content)
    }
  }
}
