package choosenearme

import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, QueryStringDecoder}
import scalaj.collection.Implicits._

case class RestApiRequest(method: HttpMethod, path: List[String], params: RestApiParameters, underlying: HttpRequest)

object RestApiRequest {
  def fromHttpRequest(request: HttpRequest): RestApiRequest = {
    val method = request.getMethod
    val decoder = new QueryStringDecoder(request.getUri)
    val params = RestApiParameters.fromDecoder(decoder)
    val path = decoder.getPath.split('/').toList.drop(1)
    RestApiRequest(method, path, params, request)
  }
}
