package choosenearme

import com.twitter.util.Base64StringEncoder
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.util.CharsetUtil.UTF_8

class TwilioApi(config: TwilioConfig) extends JsonApiClient("api.twilio.com", 443) {
  override def clientBuilder = super.clientBuilder.tls

  override def headers = super.headers ++ Map(
    HttpHeaders.Names.AUTHORIZATION ->
      ("Basic " + Base64StringEncoder.encode((config.accountSid + ":" + config.authToken).getBytes(UTF_8))))

  val prefix = "/2010-04-01/Accounts/" + config.accountSid

  def sms(to: String, body: String) = {
    val endpoint = prefix + "/SMS/Messages.json"
    val params =
      Map(
        "From" -> config.phone,
        "To" -> to,
        "Body" -> body)
    post(endpoint, params)
  }
}
