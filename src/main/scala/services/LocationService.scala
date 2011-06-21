package choosenearme

import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class LocationService(donorsChoose: DonorsChooseApi) extends RestApiService {
  override def get(request: RestApiRequest) = {
    val latlng = request.params.required[LatLng]("latlng")
    val resultFuture = donorsChoose.near(latlng)
    resultFuture map { result =>
      new RestApiResponse(JObject(List(JField("proposals", result))))
    }
  }
}
