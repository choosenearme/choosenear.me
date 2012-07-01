package choosenearme

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.util.{Duration, Future}
import java.net.URLEncoder.encode
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JValue
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{QueryStringEncoder, DefaultHttpRequest, HttpMethod, HttpHeaders}
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class JsonApiClient(host: String, port: Int = 80) {
  def clientBuilder =
    (ClientBuilder()
      .codec(Http.get)
      .tcpConnectTimeout(Duration(1, TimeUnit.SECONDS))
      .hosts(host + ":" + port)
      .hostConnectionLimit(10)
      .reportTo(new OstrichStatsReceiver))

  val client = clientBuilder.build()

  def get(endpoint: String, params: Map[String, String]): Future[JValue] = {
    val uri = {
      val encoder = new QueryStringEncoder(endpoint)
      for ((k, v) <- params)
        encoder.addParam(k, v)
      encoder.toString
    }
    call(HttpMethod.GET, uri)
  }

  def post(endpoint: String, params: Map[String, String]): Future[JValue] = {
    val body =
      (for ((k, v) <- params)
        yield encode(k, UTF_8.name) + "=" + encode(v, UTF_8.name)).mkString("&")
    call(HttpMethod.POST, endpoint, Some(body), Map(HttpHeaders.Names.CONTENT_TYPE -> "application/x-www-form-urlencoded"))
  }

  def headers = Map(
    HttpHeaders.Names.ACCEPT -> "*/*",
    HttpHeaders.Names.USER_AGENT -> "choosenear.me-api/1.0",
    HttpHeaders.Names.HOST -> host)

  def call(method: HttpMethod, uri: String, body: Option[String] = None, extraHeaders: Map[String, String] = Map()): Future[JValue] = {
    val request = new DefaultHttpRequest(HTTP_1_1, method, uri)
    for ((name, value) <- headers)
      request.addHeader(name, value)
    for ((name, value) <- extraHeaders)
      request.addHeader(name, value)
    for (content <- body) {
      val buffer = copiedBuffer(content, UTF_8)
      request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes)
      request.setContent(buffer)
    }

    client(request) map { response =>
      val content = response.getContent.toString(UTF_8)
      JsonParser.parse(content)
    }
  }
}
