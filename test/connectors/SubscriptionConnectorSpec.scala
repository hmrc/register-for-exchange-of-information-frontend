/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import cats.data.EitherT
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.SubscriptionID
import models.error.ApiError
import models.error.ApiError.{DuplicateSubmissionError, UnableToCreateEMTPSubscriptionError}
import models.subscription.request.{CreateSubscriptionForMDRRequest, DisplaySubscriptionForMDRRequest}
import models.subscription.response.SubscriptionIDResponse
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{CONFLICT, OK}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]
  private val subscriptionUrl               = "/register-for-exchange-of-information/subscription"
  private val errorCodes: Gen[Int]          = Gen.chooseNum(400, 599).suchThat(_ != 409)

  "SubscriptionConnector" - {
    "readSubscription" - {
      "must return SubscriptionResponse for valid input request" in {
        val displayMDRRequest = arbitrary[DisplaySubscriptionForMDRRequest].sample.value
        val expectedResponse  = SubscriptionID("subscriptionID")

        val subscriptionResponse: String =
          s"""
             |{
             | "displaySubscriptionForMDRResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/read-subscription", OK, subscriptionResponse)

        val result: Future[Option[SubscriptionID]] = connector.readSubscription(displayMDRRequest)
        result.futureValue.value mustBe expectedResponse
      }
    }

    "createSubscription" - {
      "must return SubscriptionResponse for valid input request" in {
        val subMDRRequest    = arbitrary[CreateSubscriptionForMDRRequest].sample.value
        val expectedResponse = SubscriptionID("subscriptionID")

        val subscriptionResponse: String =
          s"""
             |{
             | "createSubscriptionForMDRResponse": {
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

        val result = connector.createSubscription(subMDRRequest)
        result.value.futureValue mustBe Right(expectedResponse)
      }

      "must return UnableToCreateEMTPSubscriptionError for invalid response" in {
        val subMDRRequest    = arbitrary[CreateSubscriptionForMDRRequest].sample.value
        val expectedResponse = SubscriptionIDResponse("subscriptionID")

        val subscriptionResponse: String =
          s"""
             |{
             | "createSubscriptionForMDRResponse": {
             |   "responseCommon": {
             |     "status": "OK",
             |     "processingDate": "2020-09-23T16:12:11Z"
             |   },
             |   "responseDetail1": {
             |      "subscriptionID": "subscriptionID"
             |   }
             | }
             |}""".stripMargin

        stubPostResponse("/create-subscription", OK, subscriptionResponse)

        val result: EitherT[Future, ApiError, SubscriptionID] = connector.createSubscription(subMDRRequest)
        result.value.futureValue mustBe Left(UnableToCreateEMTPSubscriptionError)
      }

      "must return DuplicateSubmissionError when tried to submit the same request" in {
        val subMDRRequest = arbitrary[CreateSubscriptionForMDRRequest].sample.value

        val subscriptionErrorResponse: String =
          s"""
             | "errorDetail": {
             |    "timestamp" : "2021-03-11T08:20:44Z",
             |    "correlationId": "c181e730-2386-4359-8ee0-f911d6e5f3bc",
             |    "errorCode": "409",
             |    "errorMessage": "Duplicate submission",
             |    "source": "Back End",
             |    "sourceFaultDetail": {
             |      "detail": [
             |        "Duplicate submission"
             |      ]
             |    }
             |  }
             |""".stripMargin

        stubPostResponse("/create-subscription", CONFLICT, subscriptionErrorResponse)

        val result = connector.createSubscription(subMDRRequest)
        result.value.futureValue mustBe Left(DuplicateSubmissionError)
      }

      "must return UnableToCreateEMTPSubscriptionError when submission to backend fails" in {
        val subMDRRequest = arbitrary[CreateSubscriptionForMDRRequest].sample.value
        forAll(errorCodes) {
          errorCode =>
            val subscriptionErrorResponse: String =
              s"""
               | "errorDetail": {
               |    "timestamp": "2016-08-16T18:15:41Z",
               |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
               |    "errorCode": "$errorCode",
               |    "errorMessage": "Internal error",
               |    "source": "Internal error"
               |  }
               |""".stripMargin

            stubPostResponse("/create-subscription", errorCode, subscriptionErrorResponse)

            val result = connector.createSubscription(subMDRRequest)
            result.value.futureValue mustBe Left(UnableToCreateEMTPSubscriptionError)
        }
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
