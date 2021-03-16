package com.biddingsystem.filters

import cats.implicits.catsStdInstancesForList
import cats.{Foldable, Monoid}
import com.biddingsystem.models.CampaignProtocol.Campaign
import com.biddingsystem.models.RequestProtocol.BidRequest

trait Filter {
  def filter(bidRequest: BidRequest, campaign: Campaign): Boolean
}

case object PriceFilter extends Filter {
  def filter(bidRequest: BidRequest, campaign: Campaign) = {
    val minBidFloor = bidRequest.imp.flatMap(_.bidFloor).minOption.getOrElse(0.0)
    campaign.bid.compareTo(minBidFloor) >= 0
  }
}

case object CountryFilter extends Filter {
  def filter(bidRequest: BidRequest, campaign: Campaign) = bidRequest.device
    .flatMap(_.geo)
    .flatMap(_.country)
    .orElse(bidRequest.user.flatMap(_.geo).flatMap(_.country))
    .map(_.equals(campaign.country)).getOrElse(true)
}

case object SiteFilter extends Filter {
  override def filter(bidRequest: BidRequest, campaign: Campaign) = {
    val ids = campaign.targeting.targetedSiteIds
    if (ids.length > 0) ids.contains(bidRequest.site.id) else true
  }
}

case object SizeFilter extends Filter {
  override def filter(bidRequest: BidRequest, campaign: Campaign): Boolean = campaign.banners.find(banner => bidRequest.imp.find(
    impression => {
      val height = banner.height
      val width = banner.width
      impression.h.map(_ == height).orElse(impression.hmin.map(_ >= height).orElse(impression.hmax.map(_ <= height))).getOrElse(true) &&
        impression.w.map(_ == width).orElse(impression.wmin.map(_ >= width).orElse(impression.wmax.map(_ <= width))).getOrElse(true)
    }
  ).map(_ => true).getOrElse(true)).isDefined
}

case object Filter {
  private implicit val filterMonoid: Monoid[Filter] = new Monoid[Filter] {
    override def empty: Filter = new Filter {
      override def filter(bidRequest: BidRequest, campaign: Campaign) = true
    }

    override def combine(first: Filter, second: Filter): Filter = new Filter {
      override def filter(bidRequest: BidRequest, campaign: Campaign): Boolean =
        first.filter(bidRequest, campaign) && second.filter(bidRequest, campaign)
    }
  }

  val filter = Foldable[List].fold(
    List[Filter](PriceFilter, CountryFilter, SiteFilter, SizeFilter)
  )
}
