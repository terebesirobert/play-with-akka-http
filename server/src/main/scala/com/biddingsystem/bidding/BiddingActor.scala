package com.biddingsystem.bidding

import akka.actor.{Actor, Props}
import com.biddingsystem.models.RequestProtocol.BidRequest

case class BiddingActor(val biddingContext: CampaignContext) extends Actor {

  override def receive: Receive = {
    case bidRequest: BidRequest => {
      sender() ! biddingContext.cappingsFor(bidRequest)
    }
    case _ =>
      sender() ! List.empty
  }

}

case object BiddingActor {
  def props() = Props(BiddingActor(CampaignContext(List.empty)))
}
