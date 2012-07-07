package choosenearme

import com.twitter.finagle.Service
import com.twitter.finagle.builder.{ClientBuilder, ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.finagle.service.ProxyService
import com.twitter.util.{Duration, Future}
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import scalaj.collection.Implicits._

object ImageProxyServer {
  def main(args: Array[String]): Unit = {
    val client =
      (ClientBuilder()
        .codec(Http.get)
        .tcpConnectTimeout(Duration(1, TimeUnit.SECONDS))
        .hosts("cdn.donorschoose.net:80")
        .hostConnectionLimit(200)
        .reportTo(new OstrichStatsReceiver)
        .build())

    val fixHost = new HttpFilter[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
        request.setHeader("Host", "cdn.donorschoose.net")
        request.setUri(request.getUri.stripPrefix("/cdn"))
        service(request)
      }
    }

    val server = 
      (ServerBuilder()
        .name("choosenearme-image-proxy")
        .codec(Http.get)
        .bindTo(new InetSocketAddress(8081))
        .reportTo(new OstrichStatsReceiver)
        .build(fixHost andThen client))
  }
}
