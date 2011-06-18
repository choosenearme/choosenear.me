package me.choosenear

import com.twitter.finagle.builder.{Http, ServerBuilder}
import java.net.InetSocketAddress

object Server {
  def main(args: Array[String]): Unit = {

    val transform = new RestApiFilter
    val api = new ApiService(new DonorsChooseApi())
    val service = transform andThen api

    val server = 
      (ServerBuilder()
        .codec(Http)
        .bindTo(new InetSocketAddress(8080))
        .build(service))
  }
}
