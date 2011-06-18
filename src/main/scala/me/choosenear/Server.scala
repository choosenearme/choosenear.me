package me.choosenear

import com.mongodb.Mongo
import com.twitter.finagle.builder.{Http, ServerBuilder}
import com.twitter.util.FuturePool
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB, MongoAddress, MongoHostBase}

object Server {
  def main(args: Array[String]): Unit = {
    val _mongo = new Mongo
    val address = MongoAddress(new MongoHostBase { def mongo = _mongo }, "test")
    MongoDB.defineDb(DefaultMongoIdentifier, address)

    val mongoPool = Executors.newFixedThreadPool(4)
    val userDb = new MongoUserDb(FuturePool(mongoPool))
    val foursquareKey = "DQZ11NC45FXOB1OHZZPMIPZ0CPZBWRWLRIGGPBNGUH4FNWM5"
    val foursquareSecret = "YCPPWCY4NV3JJNB0VBOM3GNJ3GABSG5TVL2RYM0VIU0DXFZW"
    val foursquareApi = new FoursquareApi(foursquareKey, foursquareSecret)
    val foursquareAuthApi = new FoursquareAuthenticationApi("http://localhost:8080/auth/callback", foursquareKey, foursquareSecret)
    val foursquareAuthService = new FoursquareAuthenticationService(foursquareAuthApi, foursquareApi, userDb)
    val authFilter = new AuthenticationFilter(foursquareAuthService)
    val restFilter = new RestApiFilter
    val api = new ChoosenearmeApiService(new DonorsChooseApi, foursquareApi, userDb)
    val service = authFilter andThen restFilter andThen api

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .build(service))
  }
}
