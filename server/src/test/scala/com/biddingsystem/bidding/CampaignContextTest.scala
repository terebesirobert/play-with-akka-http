package com.biddingsystem.bidding

import java.util.UUID

import com.biddingsystem.models.CampaignProtocol.{Banner, Campaign, Targeting}
import com.biddingsystem.models.RequestProtocol.{BidRequest, Site}
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.GivenWhenThen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CampaignContextTest extends AnyFeatureSpecLike with ScalaCheckDrivenPropertyChecks with GivenWhenThen {

  Feature("Match bid requests and campaigns") {

    forAll(Generators.campaignsGenerator) {
      campaigns =>
        Scenario(s"Match campaigns ${UUID.randomUUID()}") {
          Given("a campaign context")
          val campaignContext = CampaignContext(
            campaigns,
            (_: BidRequest, _: Campaign) => true
          )

          When("a bid request is received with at least one matching campaign")
          val bidRequest = BidRequest("id", List.empty, Site("id", "domain"), None, None)


          Then("a new order request should be sent to the outgoing message processor")
          campaignContext.matchingCampaigns(bidRequest).length shouldBe campaigns.length
        }
    }
  }

  Feature("Don't match bid requests and campaigns") {

    forAll(Generators.campaignsGenerator) {
      campaigns =>
        Scenario(s"Don't match campaigns ${UUID.randomUUID()}") {
          Given("a campaign context")
          val campaignContext = CampaignContext(
            campaigns,
            (_: BidRequest, _: Campaign) => false
          )

          When("a bid request is received with no matching campaign")
          val bidRequest = BidRequest("id", List.empty, Site("id", "domain"), None, None)


          Then("a new order request should be sent to the outgoing message processor")
          campaignContext.matchingCampaigns(bidRequest) shouldBe List.empty
        }
    }
  }
}

case object Generators {

  import org.scalacheck.Gen

  def campaignsGenerator: Gen[List[Campaign]] = Gen.listOfN(10, campaignGenerator)

  def campaignGenerator: Gen[Campaign] =
    baseCampaignData().map((Campaign.apply _).tupled)

  type BaseCampaignData = (Int, String, Targeting, List[Banner], Double)

  def stringGenerator: Gen[String] = Gen.alphaStr.filter(_.nonEmpty)
  def intGenerator: Gen[Int] = Gen.chooseNum(0, 1000)
  def doubleGenerator: Gen[Double] = Gen.chooseNum(0.0, 1000.0)


  private def baseCampaignData(): Gen[BaseCampaignData] = for {
    id <- intGenerator
    country <- stringGenerator
    targeting <- targetingGenerator
    banners <- Gen.listOf(bannerGenerator)
    bid <- doubleGenerator.filterNot(_.compareTo(0.0) == 0)
  } yield (id, country, targeting, banners, bid)

  private def targetingGenerator: Gen[Targeting] = for {
    ids <- Gen.listOf(stringGenerator)
  } yield Targeting(ids.toVector)

  private def bannerGenerator: Gen[Banner] = for {
    id <- intGenerator
    src <- stringGenerator
    width <- intGenerator
    height <- intGenerator
  } yield Banner(id, src, width, height)
}
