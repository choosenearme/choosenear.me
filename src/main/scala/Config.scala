package choosenearme

import com.twitter.util.Eval
import java.io.File

object Config {
  def fromFile(fileName: String): Config = {
    val eval = new Eval
    eval[Config](new File(fileName))
  }
}

case class Config(mongo: MongoConfig,
                  foursquare: FoursquareConfig,
                  donorschoose: DonorsChooseConfig,
                  twilio: Option[TwilioConfig])
case class MongoConfig(name: String)
case class FoursquareConfig(key: String, secret: String, callback: String)
case class DonorsChooseConfig(key: String)
case class TwilioConfig(phone: String, accountSid: String, authToken: String)

object DevConfig extends Config(
                          MongoConfig(name = "test"),
                          FoursquareConfig(key = "DQZ11NC45FXOB1OHZZPMIPZ0CPZBWRWLRIGGPBNGUH4FNWM5",
                                           secret = "YCPPWCY4NV3JJNB0VBOM3GNJ3GABSG5TVL2RYM0VIU0DXFZW",
                                           callback = "http://localhost:8080/auth/callback"),
                          DonorsChooseConfig(key = "DONORSCHOOSE"),
                          None)
