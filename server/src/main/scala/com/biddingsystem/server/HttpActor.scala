package com.biddingsystem.server

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.biddingsystem.models.RequestProtocol.BidRequest
import com.biddingsystem.server.HttpActor.StartHttpServer
import de.heikoseeberger.akkahttpjackson.JacksonSupport


case class HttpActor(host: String, port: Int, actorRef: () => ActorRef) extends Actor with JacksonSupport {

  implicit val system = context.system
  implicit val executionContext = context.dispatcher
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  val route = path("bidding" / "api") {
    get(entity(as[BidRequest]) {
      bidRequest => {
        onSuccess(actorRef() ? bidRequest) {
          case campaigns =>
            val httpResponse = HttpResponse().withStatus(StatusCodes.OK)
              .withEntity(
                ContentTypes.`application/json`,
                JacksonSupport.defaultObjectMapper.writeValueAsString(campaigns)
              )
            complete(httpResponse)
        }
      }
    })
  }

  override def receive: Receive = {
    case StartHttpServer => Http().newServerAt(host, port).bind(route)
  }

}

case object HttpActor {

  case object StartHttpServer

  def props(host: String, port: Int, actorRef: () => ActorRef) = Props(HttpActor(host, port, actorRef))
}
