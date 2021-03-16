package com.biddingsystem.bidding

import akka.actor.{Actor, Props}
import com.biddingsystem.models.RequestProtocol.BidRequest

case class BiddingActor(biddingContext: CampaignContext) extends Actor {

  override def receive: Receive = {
    case bidRequest: BidRequest => {
      sender() ! biddingContext.matchingCampaigns(bidRequest)
    }
    case _ =>
      sender() ! List.empty
  }

}

case object BiddingActor {
  def props(context: CampaignContext) = Props(BiddingActor(context))
}
