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
import helpers.JsonFixtures.withIDResponse
import helpers.RegisterHelper._
import helpers.WireMockServerHandler
import models.register.error.ApiError.{NotFoundError, ServiceUnavailableError}
import models.register.request.details.WithIDIndividual
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
      conf = "microservice.services.register-for-exchange-of-information.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]
  val registrationUrl                       = "/register-for-exchange-of-information/registration"

  val registrationWithIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      RequestCommon("2016-08-16T15:55:30Z", "MDR", "ec031b045855445e96f98a569ds56cd2", Some(Seq(Parameters("REGIME", "MDR")))),
      RequestWithIDDetails("NINO", "0123456789", requiresNameMatch = true, isAnAgent = false, WithIDIndividual("Fred", None, "Flint", "1999-12-20"))
    )
  )

  "RegistrationConnector" - {
    "when calling registerWithID" - {

      "return 200 and a registration response when individual is matched by nino" in {

        stubResponse("/individual/id/nino", OK, withIDResponse)

        val result = connector.registerWithID(registrationWithIDPayload)
        result.value.futureValue mustBe Right(registrationWithIDResponse)
      }

      "return 404 and NotFoundError when there is no match" in {

        stubResponse("/individual/id/nino", NOT_FOUND, "{}")

        val result = connector.registerWithID(registrationWithIDPayload)
        result.value.futureValue mustBe Left(NotFoundError)
      }

      "return 503 and ServiceUnavailableError when remote is unavailable " in {

        stubResponse("/individual/id/nino", SERVICE_UNAVAILABLE, "{}")

        val result = connector.registerWithID(registrationWithIDPayload)
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
