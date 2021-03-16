package com.biddingsystem.bidding

import java.util.UUID

import com.biddingsystem.filters.Filter
import com.biddingsystem.models.CampaignProtocol.Campaign
import com.biddingsystem.models.RequestProtocol._


case class CampaignContext(campaigns: List[Campaign], filter: Filter) {

  def matchingCampaigns(bidRequest: BidRequest): List[BidResponse] =
    campaigns
      .filter(filter.filter(bidRequest, _))
      .map(
        c => BidResponse(UUID.randomUUID().toString, bidRequest.id, c.bid, c.id.toString, c.banners)
      )

}
