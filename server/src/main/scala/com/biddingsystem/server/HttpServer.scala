package com.biddingsystem.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.biddingsystem.models.RequestProtocol.BidRequest
import de.heikoseeberger.akkahttpjackson.JacksonSupport

object HttpServer extends JacksonSupport {

  implicit val actorSystem = ActorSystem(Behaviors.empty, "http-server-actor-system")
  val route: Route = (path("bidding" / "api") & post) {
    entity(as[BidRequest]) {
      bidRequest => {
        print(bidRequest)
        complete("received you")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Http().newServerAt("localhost", 1234).bind(route)
    println("Server started!")
  }
}
