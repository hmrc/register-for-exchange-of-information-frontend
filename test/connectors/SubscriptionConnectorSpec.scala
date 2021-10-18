package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.JsonFixtures.withIDResponse
import helpers.WireMockServerHandler
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]
  val subscriptionUrl                       = "/register-for-exchange-of-information/subscription"

  "SubscriptionConnector" - {
    "createSubscription" - {
      "must return SubscriptionResponse for valid input request" in {

        val

        val subscriptionResponse: String =
          s"""
             |{
             | "createSubscriptionForDACResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/create-subscription", OK, subscriptionResponse)

        val result = connector.createSubscription(registrationWithIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDResponse)
      }
    }

  }

  private def stubPostResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$subscriptionUrl$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )
}
