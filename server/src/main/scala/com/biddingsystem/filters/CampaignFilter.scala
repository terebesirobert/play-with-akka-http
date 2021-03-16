package com.biddingsystem.filters

import cats.implicits.catsStdInstancesForList
import cats.{Foldable, Monoid}
import com.biddingsystem.models.CampaignProtocol.Campaign
import com.biddingsystem.models.RequestProtocol.BidRequest

trait CampaignFilter {
  def filter(bidRequest: BidRequest, campaign: Campaign): Boolean
}

case object CountryFilter extends CampaignFilter {
  def filter(bidRequest: BidRequest, campaign: Campaign) = bidRequest.device
    .flatMap(_.geo)
    .flatMap(_.country)
    .orElse(bidRequest.user.flatMap(_.geo).flatMap(_.country))
    .map(_.equals(campaign.country)).getOrElse(false)
}

case object SiteFilter extends CampaignFilter {
  override def filter(bidRequest: BidRequest, campaign: Campaign) =
    campaign.targeting.targetedSiteIds.contains(bidRequest.site.id)
}

case object CampaignFilter {
  private implicit val filterMonoid: Monoid[CampaignFilter] = new Monoid[CampaignFilter] {
    override def empty: CampaignFilter = new CampaignFilter {
      override def filter(bidRequest: BidRequest, campaign: Campaign) = true
    }

    override def combine(first: CampaignFilter, second: CampaignFilter): CampaignFilter = new CampaignFilter {
      override def filter(bidRequest: BidRequest, campaign: Campaign): Boolean =
        first.filter(bidRequest, campaign) && second.filter(bidRequest, campaign)
    }
  }

  val campaignFilter = Foldable[List].fold(
    List[CampaignFilter](CountryFilter, SiteFilter)
  )
}
