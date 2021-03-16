package com.biddingsystem.bidding

import java.util.UUID

import com.biddingsystem.filters.{CampaignFilter, ImpressionFilter}
import com.biddingsystem.models.CampaignProtocol.Campaign
import com.biddingsystem.models.RequestProtocol._


case class CampaignContext(campaigns: List[Campaign], campaignFilter: CampaignFilter, impressionFilter: ImpressionFilter) {

  def matchingCampaigns(bidRequest: BidRequest): List[BidResponse] = bidRequest match {
    case BidRequest(_, _, _, None, None) => List.empty
    case BidRequest(_, impressions, _, _, _) if impressions.isEmpty => List.empty
    case bidRequest =>
      campaigns.filter(campaignFilter.filter(bidRequest, _))
        .flatMap(campaign => campaign.banners.map(banner => (campaign, banner)))
        .flatMap({
          case (campaign, banner) => bidRequest.imp
            .filter(impressionFilter.filter(campaign, banner, _))
            .map(impression => (campaign, banner, impression))
        }).map(_ match {
          case (campaign, banner, impression) =>
            BidResponse(
              UUID.randomUUID().toString,
              bidRequest.id,
              impression.bidFloor.getOrElse(campaign.bid),
              campaign.id.toString,
              banner
            )
      })

  }


}
