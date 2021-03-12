package com.biddingsystem.bidding

import com.biddingsystem.models.CampaignProtocol.Campaign
import com.biddingsystem.models.RequestProtocol.BidRequest

case class CampaignContext(campaigns: List[Campaign]) {

  def cappingsFor(bidRequest: BidRequest): List[Campaign] = List.empty

}
