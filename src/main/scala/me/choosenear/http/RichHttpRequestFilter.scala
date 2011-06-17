package me.choosenear

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class RichHttpRequestFilter extends HttpFilter[RichHttpRequest, HttpResponse] {
  override def apply(request: HttpRequest, service: Service[RichHttpRequest, HttpResponse]): Future[HttpResponse] = {
    service(new RichHttpRequest(request))
  }
}
