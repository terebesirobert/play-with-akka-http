package com.biddingsystem

import java.util.UUID

import akka.actor.ActorSystem
import com.biddingsystem.bidding.{BiddingActor, CampaignContext}
import com.biddingsystem.filters.{CampaignFilter, ImpressionFilter}
import com.biddingsystem.models.CampaignProtocol.{Banner, Campaign, Targeting}
import com.biddingsystem.server.HttpServerApp

import scala.util.Random

object AppMain {
  val activeCampaigns = List(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = Vector("0006a522ce0f4bbbbaa6b3c38cafaa0f") // Use collection of your choice
      ),
      banners = List(
        Banner(
          id = 1,
          src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )
  )

  val actorSystem = ActorSystem.create("http-server-actor-system")
  val campaignContext = CampaignContext(activeCampaigns, CampaignFilter.campaignFilter, ImpressionFilter.impressionFilter)
  val r = new Random

  val actorRef = () => actorSystem.actorOf(BiddingActor.props(campaignContext), s"bidding-actor-${UUID.randomUUID()}")
  val httpServer = HttpServerApp(actorSystem)("localhost", 1234, actorRef, () => r.nextInt())

  def main(args: Array[String]): Unit = {
    httpServer.startServer()
  }
}
