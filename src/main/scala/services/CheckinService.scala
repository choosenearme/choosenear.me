package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}
import net.liftweb.json.JsonParser
import org.jboss.netty.util.CharsetUtil.UTF_8

class CheckinService(foursquare: FoursquareApi, userDb: UserDb) extends RestApiService {
  implicit val formats = DefaultFormats

  override def post(request: RestApiRequest) = {
    val checkin = Future { JsonParser.parse(request.underlying.getContent.toString(UTF_8)) }
    Future.value(new RestApiResponse(JObject(List())))
  }
}
