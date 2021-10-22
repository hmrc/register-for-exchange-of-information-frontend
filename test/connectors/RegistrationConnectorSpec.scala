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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.JsonFixtures.{withIDIndividualResponse, withIDOrganisationResponse}
import helpers.RegisterHelper._
import helpers.WireMockServerHandler
import models.error.ApiError.{NotFoundError, ServiceUnavailableError}
import models.register.request.details.WithIDIndividual
import models.register.error.ApiError.{NotFoundError, ServiceUnavailableError}
import models.register.request.details.{WithIDIndividual, WithIDOrganisation}
import models.register.request.{RegisterWithID, RegisterWithIDRequest, RequestCommon, RequestWithIDDetails}
import models.shared.Parameters
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]
  val registrationUrl                       = "/register-for-exchange-of-information/registration"

  val registrationWithIndividualIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      RequestCommon("2016-08-16T15:55:30Z", "MDR", "ec031b045855445e96f98a569ds56cd2", Some(Seq(Parameters("REGIME", "MDR")))),
      RequestWithIDDetails("NINO", "0123456789", requiresNameMatch = true, isAnAgent = false, WithIDIndividual("Fred", None, "Flint", "1999-12-20"))
    )
  )

  val registrationWithOrganisationIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      RequestCommon("2016-08-16T15:55:30Z", "MDR", "ec031b045855445e96f98a569ds56cd2", Some(Seq(Parameters("REGIME", "MDR")))),
      RequestWithIDDetails("UTR", "utr", requiresNameMatch = true, isAnAgent = false, WithIDOrganisation("name", "0001"))
    )
  )

  "RegistrationConnector" - {

    "when calling withIndividualNino" - {

      "return 200 and a registration response when individual is matched by nino" in {

        stubResponse("/individual/nino", OK, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDIndividualResponse)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/individual/nino", NOT_FOUND, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/individual/nino", SERVICE_UNAVAILABLE, withIDIndividualResponse)

        val result = connector.withIndividualNino(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(ServiceUnavailableError)
      }

    }

    "when calling withOrganisationUtr" - {

      "return 200 and a registration response when organisation is matched by utr" in {

        stubResponse("/organisation/utr", OK, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithOrganisationIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDOrganisationResponse)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/organisation/utr", NOT_FOUND, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithIndividualIDPayload)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/organisation/utr", SERVICE_UNAVAILABLE, withIDOrganisationResponse)

        val result = connector.withOrganisationUtr(registrationWithOrganisationIDPayload)
        result.value.futureValue mustBe Left(ServiceUnavailableError)
      }
    }
  }

  private def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$registrationUrl$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
