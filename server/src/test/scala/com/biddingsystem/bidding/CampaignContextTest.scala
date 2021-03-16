package com.biddingsystem.bidding

import java.util.UUID

import com.biddingsystem.filters.{CampaignFilter, ImpressionFilter}
import com.biddingsystem.models.CampaignProtocol.{Banner, Campaign, Targeting}
import com.biddingsystem.models.RequestProtocol.{BidRequest, BidResponse, Device, Geo, Impression, Site, User}
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
            (_: BidRequest, _: Campaign) => true,
            (_: Campaign, _: Banner, _: Impression) => true
          )

          When("a bid request is received with at least one matching campaign")
          val bidRequest = BidRequest(
            "id",
            List(Impression("id", None, None, None, None, None, None, None)),
            Site("id", "domain"),
            Some(User("id", None)),
            None
          )

          Then("more bid responses are generated")
          campaignContext.matchingCampaigns(bidRequest).length > 0 shouldBe true
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
            (_: BidRequest, _: Campaign) => false,
            (_: Campaign, _: Banner, _: Impression) => false
          )

          When("a bid request is received with no matching campaign")
          val bidRequest = BidRequest(
            "id",
            List(Impression("id", None, None, None, None, None, None, None)),
            Site("id", "domain"),
            Some(User("id", None)),
            None
          )


          Then("no response is generated")
          campaignContext.matchingCampaigns(bidRequest) shouldBe List.empty
        }
    }
  }

  Feature("Example test") {

      Scenario(s"Don't match campaigns ${UUID.randomUUID()}") {
        Given("campaign context from example")
        val activeCampaigns = List(
          Campaign(
            id = 1,
            country = "LT",
            targeting = Targeting(
              targetedSiteIds = Vector("0006a522ce0f4bbbbaa6b3c38cafaa0f") // Use collection of your choice
            ),
            banners = List(
              Banner(
                id = 1,
                src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
                width = 300,
                height = 250
              )
            ),
            bid = 5d
          )
        )
        val campaignContext = CampaignContext(activeCampaigns, CampaignFilter.campaignFilter, ImpressionFilter.impressionFilter)

        When("the bid request from the example is received")
        val bidRequest = BidRequest(
          "SGu1Jpq1IO",
          List(Impression("1", Some(50), Some(300), Some(300), Some(100), Some(300), Some(250), Some(3.12123))),
          Site("0006a522ce0f4bbbbaa6b3c38cafaa0f", "fake.tld"),
          Some(User("USARIO1", Some(Geo(Some("LT"))))),
          Some(Device("440579f4b408831516ebd02f6e1c31b4", Some(Geo(Some("LT")))))
        )


        Then("the expected output is generated")
        val result = campaignContext.matchingCampaigns(bidRequest)
        result.length shouldBe 1
        val response: BidResponse = result(0)
        response.adId shouldBe "1"
        response.bidRequestId shouldBe "SGu1Jpq1IO"
        response.banner.id shouldBe 1
        response.banner.width shouldBe 300
        response.banner.height shouldBe 250
        response.price shouldBe 3.12123
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
