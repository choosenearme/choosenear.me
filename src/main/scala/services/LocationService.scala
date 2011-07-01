package choosenearme

import net.liftweb.json.JsonAST.{JValue, JObject, JField, JString}

class LocationService(donorsChoose: DonorsChooseApi) extends RestApiService {
  override def get(request: RestApiRequest) = {
    val latlng = request.params.required[LatLong]("latlng")

    for {
      proposalsJson <- donorsChoose.near(latlng)
    } yield new RestApiResponse(JObject(List(JField("proposals", proposalsJson))))
  }
}
