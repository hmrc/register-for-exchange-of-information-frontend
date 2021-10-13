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

package services

import base.{MockServiceApp, SpecBase}
import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.RegistrationConnector
import helpers.RegisterHelper._
import models.Name
import models.matching.MatchingInfo
import models.register.error.ApiError
import models.register.error.ApiError.NotFoundError
import models.register.response.RegistrationWithIDResponse
import models.subscription.response._
import models.subscription.{ContactInformationForOrganisation, OrganisationDetails, PrimaryContact}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessMatchingServiceSpec extends SpecBase with MockServiceApp with MockitoSugar {

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  val service: BusinessMatchingService = app.injector.instanceOf[BusinessMatchingService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector)
    )

  override def beforeEach: Unit = {
    Mockito.reset(
      mockRegistrationConnector
    )
    super.beforeEach()
  }

  val name: Name = Name("First", "Last")

  val dob: LocalDate = LocalDate.now

  val displaySubscriptionResponse: DisplaySubscriptionForCBCResponse = DisplaySubscriptionForCBCResponse(
    SubscriptionForCBCResponse(
      ResponseCommon("200", None, "2016-08-16T15:55:30Z", Some(Seq(ReturnParameters("REGIME", "MDR")))),
      ResponseDetail("subscriptionID",
                     None,
                     isGBUser = true,
                     PrimaryContact(ContactInformationForOrganisation(OrganisationDetails("name"), "test@test.org", None, None)),
                     None
      )
    )
  )

  "BusinessMatchingService" - {

    "sendIndividualMatchingInformation" - {

      "must return matching information when both safeId and subscriptionId can be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Right(registrationWithIDResponse))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, MatchingInfo]] = service.sendIndividualMatchingInformation(Nino("CC123456C"), name, dob)

        result.futureValue mustBe Right(MatchingInfo("XE0000123456789"))
      }

      "must return an error when when safeId or subscriptionId can't be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Left(NotFoundError))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, MatchingInfo]] = service.sendIndividualMatchingInformation(Nino("CC123456C"), name, dob)

        result.futureValue mustBe Left(NotFoundError)
      }
    }
  }
}
