package choosenearme

import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, QueryStringDecoder}
import scalaj.collection.Implicits._

case class RestApiRequest(method: HttpMethod, path: List[String], params: RestApiParameters, underlying: HttpRequest)

object RestApiRequest {
  def fromHttpRequest(request: HttpRequest): RestApiRequest = {
    val method = request.getMethod
    val decoder = new QueryStringDecoder(request.getUri)
    val path = decoder.getPath.split('/').toList.drop(1)
    val params = new RestApiParameters(decoder.getParameters.asScala.mapValues(_.asScala))
    RestApiRequest(method, path, params, request)
  }
}
