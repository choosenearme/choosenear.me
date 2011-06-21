package me.choosenear

import com.twitter.util.Future
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class CheckinService(foursquare: FoursquareApi, userDb: UserDb) extends RestApiService {
  implicit val formats = DefaultFormats

  override def post(request: RestApiRequest) = {
    // val secret = request.params.required[String]("secret")
    // for {
    //   user <- userDb.fetchOne(User.where(_.secret eqs secret))
    //   val api = foursquare.authenticateUser(user)
    //   checkinsInfo <- api.checkins
    // } yield {
    //   new RestApiResponse(JObject(List(JField("response", decompose(checkinsInfo)))))
    // }
    Future.exception(new Exception())
  }
}
