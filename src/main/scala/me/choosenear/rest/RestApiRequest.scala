package me.choosenear

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}
import scalaj.collection.Implicits._

class RestApiRequest(val request: HttpRequest) {
  lazy val method = request.getMethod
  lazy val (path, params) = {
    val decoder = new QueryStringDecoder(request.getUri)
    val _path = decoder.getPath.split('/').toList.drop(1)
    val _params = decoder.getParameters.asScala.mapValues(_.asScala)
    (_path, new RestApiParameters(_params))
  }
}
