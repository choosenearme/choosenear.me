package choosenearme

import com.twitter.util.Future
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString, JInt, JArray, JDouble}

class CitiesService(db: Db, foursquare: FoursquareApi) extends RestApiService {
  private def getCities(user: User)(cities: List[City]): Future[List[City]] = cities match {
    case Nil =>
      val api = foursquare.authenticateUser(user)
      for {
        allCheckins <- api.allCheckins
      } yield {
        val dbCheckins = allCheckins.flatMap(Checkin.fromCheckinDetail(user))
        val cities = City.citiesForCheckins(user)(allCheckins)
        db.insertAll(dbCheckins)
        db.insertAll(cities)
        cities
      }
    case cs => Future(cs)
  }

  override def get(request: RestApiRequest) = {
    val secret = request.params.required[String]("secret")

    for {
      user <- db.fetchOne(User.where(_.secret eqs secret))
      cities <- db.fetch(City.where(_.userId eqs user.foursquareId.value).orderDesc(_.numCheckins)).flatMap(getCities(user))
    } yield {
      val fields =
        for (city <- cities)
          yield JObject(List(
            JField("id", JString(city.id.toString)),
            JField("name", JString(city.name.value)),
            JField("lat", JDouble(city.latlng.lat)),
            JField("lng", JDouble(city.latlng.long)),
            JField("count", JInt(city.numCheckins.value))))

      new RestApiResponse(JObject(List(JField("response", JArray(fields)))))
    }
  }
}
