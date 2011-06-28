package choosenearme

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class CategoriesService(foursquare: FoursquareApi,
                        db: Db) extends RestApiService {
  implicit val formats = DefaultFormats

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")
    for {
      user <- db.fetchOne(User.where(_.secret eqs secret))
      val api = foursquare.authenticateUser(user)
      categoriesInfo <- api.categories
    } yield
      new RestApiResponse(JObject(List(JField("response", decompose(categoriesInfo)))))
  }
}