package choosenearme

import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, NOT_FOUND}

case class RestApiException(message: String, status: HttpResponseStatus = BAD_REQUEST) extends RuntimeException(message)

object RestApiNotFoundException extends RestApiException("", NOT_FOUND)
