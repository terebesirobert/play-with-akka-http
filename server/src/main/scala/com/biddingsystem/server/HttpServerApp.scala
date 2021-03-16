package com.biddingsystem.server

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.biddingsystem.models.RequestProtocol.{BidRequest, BidResponse}
import de.heikoseeberger.akkahttpjackson.JacksonSupport

case class HttpServerApp(actorSystem: ActorSystem)
  (host: String, port: Int, actorRef: () => ActorRef, randomGenerator: () => Int) extends JacksonSupport {

  implicit val system = actorSystem
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  val route = path("bidding" / "api") {
    get(entity(as[BidRequest]) {
      bidRequest => {
        onSuccess(actorRef() ? bidRequest) {
          case campaigns: List[BidResponse] if campaigns.isEmpty =>
            complete(HttpResponse().withStatus(StatusCodes.NoContent))
          case campaigns: List[BidResponse] =>
            val randomIndex = randomGenerator()
            val response = campaigns(if (randomIndex > campaigns.length) campaigns.length -1  else randomIndex)
            val httpResponse = HttpResponse().withStatus(StatusCodes.OK)
              .withEntity(
                ContentTypes.`application/json`,
                JacksonSupport.defaultObjectMapper.writeValueAsString(response)
              )
            complete(httpResponse)
        }
      }
    })
  }

  def startServer() = {
    Http().newServerAt(host, port).bind(route)
  }

}
