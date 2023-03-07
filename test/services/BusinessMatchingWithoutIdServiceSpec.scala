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

package services

import base.{MockServiceApp, SpecBase}
import connectors.RegistrationConnector
import helpers.RegisterHelper._
import models.error.ApiError
import models.error.ApiError.NotFoundError
import models.matching.SafeId
import models.{Address, Country, Name}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessMatchingWithoutIdServiceSpec extends SpecBase with MockServiceApp with MockitoSugar {

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  val service: BusinessMatchingWithoutIdService = app.injector.instanceOf[BusinessMatchingWithoutIdService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector)
    )

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRegistrationConnector
    )
    super.beforeEach()
  }

  val name: Name = Name("First", "Last")

  val dob: LocalDate = LocalDate.now

  val address: Address = Address("line 1", Some("line 2"), "line 3", Some("line 4"), Some(""), Country.GB)

  "BusinessMatchingWithoutIdService" - {

    "sendIndividualRegistration" - {

      "must return matching information when safeId can be recovered" in {

        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(SafeId("XE0000123456789")))

        when(mockRegistrationConnector.withIndividualNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendIndividualRegistration(name, dob, address, contactDetails)

        result.futureValue mustBe Right(SafeId("XE0000123456789"))
      }

      "must return an error when when safeId can't be recovered" in {

        val response: Future[Left[ApiError, SafeId]] = Future.successful(Left(NotFoundError))

        when(mockRegistrationConnector.withIndividualNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendIndividualRegistration(name, dob, address, contactDetails)

        result.futureValue mustBe Left(NotFoundError)
      }
    }

    "sendBusinessRegistration" - {

      "must return matching information when safeId can be recovered" in {

        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(SafeId("XE0000123456789")))

        when(mockRegistrationConnector.withOrganisationNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendBusinessRegistration("name", address, contactDetails)

        result.futureValue mustBe Right(SafeId("XE0000123456789"))
      }

      "must return an error when when safeId can't be recovered" in {

        val response: Future[Left[ApiError, SafeId]] = Future.successful(Left(NotFoundError))

        when(mockRegistrationConnector.withOrganisationNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendBusinessRegistration("name", address, contactDetails)

        result.futureValue mustBe Left(NotFoundError)
      }
    }
  }
}
