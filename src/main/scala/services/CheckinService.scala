package choosenearme

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class CheckinService(foursquare: FoursquareApi, userDb: UserDb) extends RestApiService {
  implicit val formats = DefaultFormats

  override def post(request: RestApiRequest) = {
    println(request.underlying)
    Future.value(new RestApiResponse(JObject(List())))
  }
}
