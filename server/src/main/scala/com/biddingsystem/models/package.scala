package com.biddingsystem

import com.biddingsystem.models.CampaignProtocol.Banner

package object models {

  object CampaignProtocol {
    case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
    case class Targeting(targetedSiteIds: Vector[String])
    case class Banner(id: Int, src: String, width: Int, height: Int)
  }

  object RequestProtocol {
    case class BidRequest(id: String, imp: List[Impression], site: Site, user: Option[User], device: Option[Device])
    case class BidResponse(id: String, bidRequestId: String, price: Double, adId: String, banner: List[Banner])
    case class Impression(
      id: String,
      wmin: Option[Int],
      wmax: Option[Int],
      w: Option[Int],
      hmin: Option[Int],
      hmax: Option[Int],
      h: Option[Int],
      bidFloor: Option[Double]
    )
    case class Site(id: String, domain: String)
    case class User(id: String, geo: Option[Geo])
    case class Device(id: String, geo: Option[Geo])
    case class Geo(country: Option[String])
  }
}
