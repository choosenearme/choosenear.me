package choosenearme

import com.mongodb.Mongo
import com.twitter.finagle.builder.{Http, ServerBuilder}
import com.twitter.util.FuturePool
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB, MongoAddress, MongoHostBase}

object Server {
  def main(args: Array[String]): Unit = {
    val config =
      (Option(System.getProperty("choosenearme.config")).map(Config.fromFile).getOrElse(DevConfig))

    val _mongo = new Mongo
    val address = MongoAddress(new MongoHostBase { def mongo = _mongo }, config.mongo.name)
    MongoDB.defineDb(DefaultMongoIdentifier, address)

    val mongoPool = Executors.newFixedThreadPool(4)
    val userDb = new MongoUserDb(FuturePool(mongoPool))
    val foursquareApi = new FoursquareApi
    val foursquareAuthApi = new FoursquareAuthenticationApi(config.foursquare)
    val foursquareAuthService = new FoursquareAuthenticationService(foursquareAuthApi, foursquareApi, userDb)
    val authFilter = new AuthenticationFilter(foursquareAuthService)
    val restFilter = new RestApiFilter
    val locationService = new LocationService(new DonorsChooseApi(config.donorschoose))
    val userService = new UserService(foursquareApi, userDb)
    val checkinsService = new CheckinsService(foursquareApi, userDb)
    val checkinService = new CheckinService(foursquareApi, userDb)

    val service = authFilter andThen restFilter andThen RestApiRouter {
      case "location" :: Nil => locationService
      case "user" :: Nil => userService
      case "checkins" :: Nil => checkinsService
      case "checkin" :: Nil => checkinService
    }

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .build(service))
  }
}
