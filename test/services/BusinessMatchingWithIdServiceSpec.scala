/*
 * Copyright 2022 HM Revenue & Customs
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
import models.BusinessType.LimitedCompany
import models.error.ApiError
import models.error.ApiError.NotFoundError
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, RegistrationInfo, RegistrationRequest}
import models.register.response.RegistrationWithIDResponse
import models.{MDR, Name}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessMatchingWithIdServiceSpec extends SpecBase with MockServiceApp with MockitoSugar {

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  val service: BusinessMatchingWithIdService = app.injector.instanceOf[BusinessMatchingWithIdService]

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

  "BusinessMatchingWithIdService" - {

    "sendIndividualRegistrationInformation" - {

      "must return matching information when both safeId and subscriptionId can be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Right(registrationWithIDIndividualResponse))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendIndividualRegistrationInformation(MDR, RegistrationRequest("NINO", "CC123456C", name.fullName, None))

        result.futureValue mustBe Right(IndRegistrationInfo("XE0000123456789"))
      }

      "must return an error when when safeId or subscriptionId can't be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Left(NotFoundError))

        when(mockRegistrationConnector.withIndividualNino(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendIndividualRegistrationInformation(MDR, RegistrationRequest("UTR", "CC123456C", name.fullName, None))

        result.futureValue mustBe Left(NotFoundError)
      }
    }

    "sendBusinessRegistrationInformation" - {

      "must return matching information when both safeId and subscriptionId can be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Right(registrationWithIDOrganisationResponse))

        when(mockRegistrationConnector.withOrganisationUtr(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendBusinessRegistrationInformation(
            MDR,
            RegistrationRequest("UTR", "XE0000123456789", "name", Some(LimitedCompany))
          )

        result.futureValue mustBe Right(
          OrgRegistrationInfo("XE0000123456789", "name", addressResponse)
        )
      }

      "must return an error when when safeId or subscriptionId can't be recovered" in {

        val response: EitherT[Future, ApiError, RegistrationWithIDResponse] = EitherT.fromEither[Future](Left(NotFoundError))

        when(mockRegistrationConnector.withOrganisationUtr(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, RegistrationInfo]] =
          service.sendBusinessRegistrationInformation(
            MDR,
            RegistrationRequest("UTR", "UTR", "name", Some(LimitedCompany))
          )

        result.futureValue mustBe Left(NotFoundError)
      }
    }
  }
}
