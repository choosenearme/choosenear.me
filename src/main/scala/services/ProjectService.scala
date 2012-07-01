package choosenearme

import net.liftweb.json.JsonAST.{JObject}

class ProjectService(donorsChoose: DonorsChooseApi, id: String) extends RestApiService {
  override def get(request: RestApiRequest) = {
    for (json <- donorsChoose.projectInfo(id))
      yield new RestApiResponse(json.asInstanceOf[JObject])
  }
}
