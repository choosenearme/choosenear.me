package me.choosenear

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}
import scalaj.collection.Implicits._

class RichHttpRequest(val request: HttpRequest) {
  lazy val method = request.getMethod
  lazy val decoder = new QueryStringDecoder(request.getUri)
  lazy val path = decoder.getPath.split('/').toList.drop(1)
  lazy val parameters = decoder.getParameters.asScala.mapValues(_.asScala)
}
