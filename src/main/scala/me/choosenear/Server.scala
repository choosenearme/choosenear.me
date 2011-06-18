package me.choosenear

import com.twitter.finagle.builder.{Http, ServerBuilder}
import java.net.InetSocketAddress

object Server {
  def main(args: Array[String]): Unit = {
    val restFilter = new RestApiFilter
    val api = new ChoosenearmeApiService(new DonorsChooseApi())
    val service = restFilter andThen api

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .build(service))
  }
}
