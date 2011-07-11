package choosenearme

import com.mongodb.Mongo
import com.twitter.finagle.builder.{Http, ServerBuilder}
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.config.{AdminServiceConfig, StatsConfig, TimeSeriesCollectorConfig}
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

    // val httpLogger = Logger.getLogger("http")
    // httpLogger.setLevel(Level.ALL)

    val mongoPool = Executors.newFixedThreadPool(4)
    val db = new MongoDb(FuturePool(mongoPool))

    val ostrichAdmin = new AdminServiceConfig {
      httpPort = 22557
      statsNodes = new StatsConfig {
        reporters = new TimeSeriesCollectorConfig
      }
    }
    val runtime = RuntimeEnvironment(this, Array())
    val ostrich = ostrichAdmin()(runtime)

    val foursquare = new FoursquareApi
    val foursquareAuth = new FoursquareAuthenticationApi(config.foursquare)
    val donorschoose = new DonorsChooseApi(config.donorschoose)
    val twilio = config.twilio.map(new TwilioApi(_))

    val restFilter = new RestApiFilter

    val authService = new FoursquareAuthenticationService(foursquareAuth, foursquare, db)
    val locationService = new LocationService(donorschoose)
    val userService = new UserService(foursquare, db)
    val checkinsService = new CheckinsService(foursquare, db)
    val checkinService = new CheckinService(donorschoose, foursquare, twilio, db)
    val categoriesService = new CategoriesService(foursquare, db)
    val pingService = new PingService
    val citiesService = new CitiesService(db, foursquare)
    val cityService = new CityService(db, foursquare, donorschoose)

    val service = restFilter andThen RestApiRouter {
      case "auth" :: _ => authService
      case "categories" :: Nil => categoriesService
      case "checkin" :: Nil => checkinService
      case "checkins" :: Nil => checkinsService
      case "cities" :: Nil => citiesService
      case "city" :: Nil => cityService
      case "location" :: Nil => locationService
      case "ping" :: Nil => pingService
      case "user" :: Nil => userService
    }

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .reportTo(new OstrichStatsReceiver)
        .build(service))
  }
}
