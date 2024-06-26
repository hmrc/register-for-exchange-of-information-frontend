/*
 * Copyright 2023 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, put, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.IdentifierType._
import models.enrolment.{SubscriptionInfo, Verifier}
import models.error.ApiError
import models.error.ApiError.{ServiceUnavailableError, UnableToCreateEnrolmentError}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxEnrolmentsConnectorSpec
    extends SpecBase
    with WireMockServerHandler
    with Generators
    with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.tax-enrolments.port" -> server.port()
    )
    .build()

  lazy val connector: TaxEnrolmentsConnector = app.injector.instanceOf[TaxEnrolmentsConnector]

  "TaxEnrolmentsConnector" - {

    "createEnrolment" - {

      "must return status as 204 for successful Tax Enrolment call" in {
        forAll(validSafeID, validSubscriptionID, validUtr) { (safeID, subID, utr) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, saUtr = Some(utr), mdrId = subID)

          stubResponseForPutRequest("/tax-enrolments/service/HMRC-MDR-ORG/enrolment", NO_CONTENT)

          val result: EitherT[Future, ApiError, Int] = connector.createEnrolment(enrolmentInfo)
          result.value.futureValue mustBe Right(NO_CONTENT)
        }
      }

      "must return status as 400 and BadRequest error" in {
        forAll(validSafeID, validSubscriptionID, validUtr) { (safeID, subID, utr) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, saUtr = Some(utr), mdrId = subID)
          stubResponseForPutRequest("/tax-enrolments/service/HMRC-MDR-ORG/enrolment", BAD_REQUEST)

          val result = connector.createEnrolment(enrolmentInfo)
          result.value.futureValue mustBe Left(UnableToCreateEnrolmentError)
        }
      }

      "must return status ServiceUnavailable Error" in {
        forAll(validSafeID, validSubscriptionID, validUtr) { (safeID, subID, utr) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, saUtr = Some(utr), mdrId = subID)
          stubResponseForPutRequest("/tax-enrolments/service/HMRC-MDR-ORG/enrolment", INTERNAL_SERVER_ERROR)

          val result = connector.createEnrolment(enrolmentInfo)
          result.value.futureValue mustBe Left(ServiceUnavailableError)
        }
      }
    }

    "createEnrolmentRequest" - {

      "must return correct EnrolmentRequest nino provided" in {
        forAll(validSafeID, validSubscriptionID, validNino) { (safeID, subID, nino) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, nino = Some(nino), mdrId = subID)

          val expectedVerifiers = Seq(Verifier(SAFEID, enrolmentInfo.safeID), Verifier(NINO, enrolmentInfo.nino.get))

          enrolmentInfo.convertToEnrolmentRequest.verifiers mustBe expectedVerifiers

        }
      }
      "must return correct EnrolmentRequest when saUtr provided as verifier" in {

        forAll(validSafeID, validSubscriptionID, validUtr) { (safeID, subID, utr) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, saUtr = Some(utr), mdrId = subID)

          val expectedVerifiers = Seq(Verifier(SAFEID, enrolmentInfo.safeID), Verifier(SAUTR, enrolmentInfo.saUtr.get))

          enrolmentInfo.convertToEnrolmentRequest.verifiers mustBe expectedVerifiers
        }
      }

      "must return correct EnrolmentRequest when ctUtr provided as verifier" in {

        forAll(validSafeID, validSubscriptionID, validUtr) { (safeID, subID, utr) =>
          val enrolmentInfo = SubscriptionInfo(safeID = safeID, ctUtr = Some(utr), mdrId = subID)

          val expectedVerifiers = Seq(Verifier(SAFEID, enrolmentInfo.safeID), Verifier(CTUTR, enrolmentInfo.ctUtr.get))

          enrolmentInfo.convertToEnrolmentRequest.verifiers mustBe expectedVerifiers
        }
      }
    }
  }

  private def stubResponseForPutRequest(expectedUrl: String, expectedStatus: Int): StubMapping =
    server.stubFor(
      put(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
