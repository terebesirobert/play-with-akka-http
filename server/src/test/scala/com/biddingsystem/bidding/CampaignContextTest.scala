package com.biddingsystem.bidding

import java.util.UUID

import com.biddingsystem.models.CampaignProtocol.{Banner, Campaign, Targeting}
import com.biddingsystem.models.RequestProtocol.{BidRequest, Site}
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{GivenWhenThen, Inside}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CampaignContextTest extends AnyFeatureSpecLike with TableDrivenPropertyChecks with ScalaCheckDrivenPropertyChecks with GivenWhenThen with Inside {

  Feature("Match bid requests and campaigns") {

    forAll(Generators.campaignsGenerator) {
      campaigns =>
        Scenario(s"Match campaigns ${UUID.randomUUID()}") {
          Given("a campaign context")
          val campaignContext = CampaignContext(campaigns)

          When("a bid request is received")
          val bidRequest = BidRequest("id", None, Site("name", "domain"), Option.empty, Option.empty)


          Then("a new order request should be sent to the outgoing message processor")
          campaignContext.cappingsFor(bidRequest) shouldEqual List(1)
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

  def stringGenerator: Gen[String] = Gen.alphaNumStr.filter(_.nonEmpty)
  def intGenerator: Gen[Int] = Gen.chooseNum(0, 1000)
  def doubleGenerator: Gen[Double] = Gen.chooseNum(0.0, 1000.0)


  private def baseCampaignData(): Gen[BaseCampaignData] = for {
    id <- intGenerator
    country <- stringGenerator
    targeting <- targetingGenerator
    banners <- Gen.listOf(bannerGenerator)
    bid <- doubleGenerator
  } yield (id, country, targeting, banners, bid)

  private def targetingGenerator: Gen[Targeting] = for {
    ids <- Gen.listOf(intGenerator)
  } yield Targeting(ids.toVector)

  private def bannerGenerator: Gen[Banner] = for {
    id <- intGenerator
    src <- stringGenerator
    width <- intGenerator
    height <- intGenerator
  } yield Banner(id, src, width, height)
}
