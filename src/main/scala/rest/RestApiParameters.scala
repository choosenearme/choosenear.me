package choosenearme

import org.jboss.netty.handler.codec.http.QueryStringDecoder
import scala.collection.Map
import scalaj.collection.Implicits._

class MissingParameterException(parameterName: String)
  extends RestApiException("Missing required parameter: " + parameterName)
class InvalidParameterException(parameterName: String, parameterValue: String)
  extends RestApiException("Invalid parameter \""+parameterName+"\" with value: " + parameterValue)

class RestApiParameters(val all: Map[String, Seq[String]]) {
  def many[T](name: String)(implicit parse: Parser[T]): Seq[T] =
    all.getOrElse(name, Nil).flatMap(parse.apply _)

  def optional[T](name: String)(implicit parse: Parser[T]): Option[T] =
    all.get(name).flatMap(xs => parse(xs.mkString(",")))

  def required[T](name: String)(implicit parse: Parser[T]): T = {
    val value = all.getOrElse(name, throw new MissingParameterException(name)).mkString(",")
    parse(value).getOrElse(throw new InvalidParameterException(name, value))
  }
}

object RestApiParameters {
  def fromDecoder(decoder: QueryStringDecoder): RestApiParameters =
    new RestApiParameters(Map() ++ decoder.getParameters.asScala.mapValues(_.asScala))
}
