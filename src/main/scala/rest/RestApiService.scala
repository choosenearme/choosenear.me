package choosenearme

import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpMethod, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND

class RestApiService extends Service[RestApiRequest, RestApiResponse] {
  override def apply(request: RestApiRequest): Future[RestApiResponse] = request match {
    case RestApiRequest(HttpMethod.GET, _, _, _) => get(request)
    case RestApiRequest(HttpMethod.PUT, _, _, _) => put(request)
    case RestApiRequest(HttpMethod.POST, _, _, _) => post(request)
    case RestApiRequest(HttpMethod.DELETE, _, _, _) => delete(request)
  }

  def get(request: RestApiRequest) = notFound(request)
  def put(request: RestApiRequest) = notFound(request)
  def post(request: RestApiRequest) = notFound(request)
  def delete(request: RestApiRequest) = notFound(request)

  def notFound(request: RestApiRequest): Future[RestApiResponse] =
    Future.exception(RestApiNotFoundException)
}
