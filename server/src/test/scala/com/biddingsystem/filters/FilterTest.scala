package com.biddingsystem.filters

import com.biddingsystem.models.CampaignProtocol.{Campaign, Targeting}
import com.biddingsystem.models.RequestProtocol._
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FilterTest extends AnyFeatureSpecLike with ScalaCheckDrivenPropertyChecks with GivenWhenThen {


  Feature("Test country filter in") {
    Scenario("Filter in campaigns that match device country requirements") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        None,
        Some(Device("id", Some(geo)))
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should be good")
      result shouldBe true
    }

    Scenario("Filter in campaigns that match user country requirements") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        Some(User("id", Some(geo))),
        None
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should be good")
      result shouldBe true
    }

    Scenario("Filter in campaigns that match both user and device country requirements") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        Some(User("id", Some(geo))),
        Some(Device("id", Some(geo)))
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should be good")
      result shouldBe true
    }

    Scenario("Filter in campaigns if no country specified on request") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        None,
        None
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should be good")
      result shouldBe false
    }

    Scenario("Filter out campaigns if device country specified on request does not match") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country-not-good"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        None,
        Some(Device("id", Some(geo)))
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should not be good")
      result shouldBe false
    }

    Scenario("Filter out campaigns if user country specified on request does not match") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country-not-good"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        Some(User("id", Some(geo))),
        None
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should not be good")
      result shouldBe false
    }

    Scenario("Filter out campaigns if both user and device country specified on request does not match") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country-not-good"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("id", "domain"),
        Some(User("id", Some(geo))),
        Some(Device("id", Some(geo)))
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should not be good")
      result shouldBe false
    }
  }

  Feature("Test target ids filter in") {
    Scenario("Filter in campaigns that contain site as targeted site") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("siteid", "domain"),
        None,
        None
      )

      When("the filter is applied")
      val result = SiteFilter.filter(bidRequest, campaign)

      Then("the country should be good")
      result shouldBe true
    }

    Scenario("Filter in campaigns that does not contain site as targeted site") {
      Given("a campaign and a bid request")
      val campaign = Campaign(1, "country", Targeting(Vector("siteid")), List.empty, 5.0)
      val geo = Geo(Some("country-not-good"))
      val bidRequest = BidRequest(
        "id",
        List(Impression("id", None, None, None, None, None, None, Some(2.0))),
        Site("not-targeted-id", "domain"),
        Some(User("id", Some(geo))),
        Some(Device("id", Some(geo)))
      )

      When("the filter is applied")
      val result = CountryFilter.filter(bidRequest, campaign)

      Then("the country should not be good")
      result shouldBe false
    }
  }

}
