package choosenearme

import com.mongodb.Mongo
import com.twitter.finagle.builder.{Http, ServerBuilder}
import com.twitter.util.FuturePool
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.logging.{Level, Logger}
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB, MongoAddress, MongoHostBase}

object Server {
  def main(args: Array[String]): Unit = {
    val config =
      (Option(System.getProperty("choosenearme.config")).map(Config.fromFile).getOrElse(DevConfig))

    val _mongo = new Mongo
    val address = MongoAddress(new MongoHostBase { def mongo = _mongo }, config.mongo.name)
    MongoDB.defineDb(DefaultMongoIdentifier, address)

    // val httpLogger = Logger.getLogger("http")
    // httpLogger.setLevel(Level.ALL)

    val mongoPool = Executors.newFixedThreadPool(4)
    val userDb = new MongoUserDb(FuturePool(mongoPool))

    val foursquareApi = new FoursquareApi
    val foursquareAuthApi = new FoursquareAuthenticationApi(config.foursquare)
    val donorschooseApi = new DonorsChooseApi(config.donorschoose)
    val twilioApi = config.twilio.map(new TwilioApi(_))

    val restFilter = new RestApiFilter

    val authService = new FoursquareAuthenticationService(foursquareAuthApi, foursquareApi, userDb)
    val locationService = new LocationService(donorschooseApi)
    val userService = new UserService(foursquareApi, userDb)
    val checkinsService = new CheckinsService(foursquareApi, userDb)
    val checkinService = new CheckinService(donorschooseApi, foursquareApi, twilioApi, userDb)
    val categoriesService = new CategoriesService(foursquareApi, userDb)
    val pingService = new PingService

    val service = restFilter andThen RestApiRouter {
      case "auth" :: _ => authService
      case "location" :: Nil => locationService
      case "user" :: Nil => userService
      case "checkins" :: Nil => checkinsService
      case "checkin" :: Nil => checkinService
      case "categories" :: Nil => categoriesService
      case "ping" :: Nil => pingService
    }

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .build(service))
  }
}
