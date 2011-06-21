package choosenearme

import com.twitter.finagle.builder.{ClientBuilder, Http}
import com.twitter.util.Future
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JValue
import org.jboss.netty.handler.codec.http.{QueryStringEncoder, DefaultHttpRequest}
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.util.CharsetUtil.UTF_8

class JsonApiClient(host: String, port: Int = 80) {
  def clientBuilder =
    (ClientBuilder()
      .codec(Http)
      .hosts(host + ":" + port)
      .hostConnectionLimit(10))

  val client = clientBuilder.build()

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
      // println("API call to "+host+":"+port+uri+" gives response:\n\n"+content+"\n\n")
      // println(response)
      JsonParser.parse(content)
    }
  }
}
