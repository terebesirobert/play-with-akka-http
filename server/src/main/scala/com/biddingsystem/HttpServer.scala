package com.biddingsystem

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import com.biddingsystem.bidding.BiddingActor
import com.biddingsystem.server.HttpActor
import de.heikoseeberger.akkahttpjackson.JacksonSupport

object HttpServer extends JacksonSupport {

  val actorSystem = ActorSystem.create("http-server-actor-system")

  val actorRef = () => actorSystem.actorOf(BiddingActor.props(), s"bidding-actor-${UUID.randomUUID()}")
  val httpActor = actorSystem.actorOf(HttpActor.props("localhost", 1234, actorRef), "main-actor-system")

  def main(args: Array[String]): Unit = {
    httpActor ! HttpActor.StartHttpServer
  }
}
