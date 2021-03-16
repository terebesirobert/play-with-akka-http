package com.biddingsystem.filters

import cats.implicits.catsStdInstancesForList
import cats.{Foldable, Monoid}
import com.biddingsystem.models.CampaignProtocol.{Banner, Campaign}
import com.biddingsystem.models.RequestProtocol.{BidRequest, Impression}

trait ImpressionFilter {
  def filter(campaign: Campaign, banner: Banner, impression: Impression): Boolean
}

case object PriceFilter extends ImpressionFilter {
  override def filter(campaign: Campaign, banner: Banner, impression: Impression): Boolean =
    impression.bidFloor.map(_.compareTo(campaign.bid) <= 0).getOrElse(false)
}

case object SizeFilter extends ImpressionFilter {
  override def filter(campaign: Campaign, banner: Banner, impression: Impression): Boolean = {
    val height = banner.height
    val width = banner.width
    impression.h.map(_ == height).orElse(impression.hmin.map(_ <= height)).orElse(impression.hmax.map(_ >= height)).getOrElse(false) &&
    impression.w.map(_ == width).orElse(impression.wmin.map(_ <= width)).orElse(impression.wmax.map(_ >= width)).getOrElse(false)
  }
}

case object ImpressionFilter {
  private implicit val filterMonoid: Monoid[ImpressionFilter] = new Monoid[ImpressionFilter] {
    override def empty: ImpressionFilter = new ImpressionFilter {
      override def filter(campaign: Campaign, banner: Banner, impression: Impression) = true
    }

    override def combine(first: ImpressionFilter, second: ImpressionFilter): ImpressionFilter = new ImpressionFilter {
      override def filter(campaign: Campaign, banner: Banner, impression: Impression): Boolean =
        first.filter(campaign, banner, impression) && second.filter(campaign, banner, impression)
    }
  }

  val impressionFilter = Foldable[List].fold(
    List[ImpressionFilter](PriceFilter, SizeFilter)
  )
}
