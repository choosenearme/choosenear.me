package choosenearme

import com.twitter.finagle.Service
import com.twitter.util.Future

case class RestApiRouter(routes: PartialFunction[List[String], Service[RestApiRequest, RestApiResponse]]) extends Service[RestApiRequest, RestApiResponse] {
  override def apply(request: RestApiRequest): Future[RestApiResponse] = {
    if (routes.isDefinedAt(request.path))
      Future(routes(request.path)(request)).flatMap(identity)
    else
      Future.exception(RestApiNotFoundException)
  }
}
