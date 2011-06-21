package choosenearme

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class CheckinsService(foursquare: FoursquareApi, userDb: UserDb) extends RestApiService {
  implicit val formats = DefaultFormats

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")
    for {
      user <- userDb.fetchOne(User.where(_.secret eqs secret))
      val api = foursquare.authenticateUser(user)
      checkinsInfo <- api.checkins
    } yield {
      new RestApiResponse(JObject(List(JField("response", decompose(checkinsInfo)))))
    }
  }
}
