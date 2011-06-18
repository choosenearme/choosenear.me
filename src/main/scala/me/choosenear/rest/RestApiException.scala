package me.choosenear

import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST

case class RestApiException(message: String, status: HttpResponseStatus = BAD_REQUEST) extends RuntimeException(message)
