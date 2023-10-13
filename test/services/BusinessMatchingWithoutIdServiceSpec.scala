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
import models.requests.DataRequest
import models.{Address, Country}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

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

  val dob: LocalDate = LocalDate.now

  val address: Address = Address("line 1", Some("line 2"), "line 3", Some("line 4"), Some(""), Country.GB)

  "BusinessMatchingWithoutIdService" - {

    "registerWithoutId" - {

      "must return a safeId when individual information can be matched" in {
        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(safeId))

        when(mockRegistrationConnector.withIndividualNoId(any())(any(), any())).thenReturn(response)

        val userAnswers = emptyUserAnswers
          .set(DoYouHaveNINPage, false)
          .success
          .value
          .set(NonUkNamePage, nonUkName)
          .success
          .value
          .set(DateOfBirthWithoutIdPage, dob)
          .success
          .value
          .set(IndividualContactPhonePage, TestPhoneNumber)
          .success
          .value
          .set(IndividualContactEmailPage, TestEmail)
          .success
          .value
          .set(IndividualAddressWithoutIdPage, address)
          .success
          .value

        val request: DataRequest[AnyContent]         = DataRequest(FakeRequest(), emptyUserAnswers.id, Individual, userAnswers)
        val result: Future[Either[ApiError, SafeId]] = service.registerWithoutId()(request, hc)

        result.futureValue mustBe Right(safeId)
      }

      "must return a safeId when business information can be matched" in {
        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(safeId))

        when(mockRegistrationConnector.withOrganisationNoId(any())(any(), any())).thenReturn(response)

        val userAnswers = emptyUserAnswers
          .set(BusinessWithoutIDNamePage, OrgName)
          .success
          .value
          .set(ContactPhonePage, TestPhoneNumber)
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value
          .set(BusinessAddressWithoutIdPage, address)
          .success
          .value

        val request: DataRequest[AnyContent]         = DataRequest(FakeRequest(), emptyUserAnswers.id, Organisation, userAnswers)
        val result: Future[Either[ApiError, SafeId]] = service.registerWithoutId()(request, hc)

        result.futureValue mustBe Right(safeId)
      }
    }

    "sendIndividualRegistration" - {

      "must return matching information when safeId can be recovered" in {

        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(safeId))

        when(mockRegistrationConnector.withIndividualNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendIndividualRegistration(nonUkName.toName, dob, address, contactDetails)

        result.futureValue mustBe Right(safeId)
      }

      "must return an error when when safeId can't be recovered" in {

        val response: Future[Left[ApiError, SafeId]] = Future.successful(Left(NotFoundError))

        when(mockRegistrationConnector.withIndividualNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendIndividualRegistration(nonUkName.toName, dob, address, contactDetails)

        result.futureValue mustBe Left(NotFoundError)
      }
    }

    "sendBusinessRegistration" - {

      "must return matching information when safeId can be recovered" in {

        val response: Future[Either[ApiError, SafeId]] = Future.successful(Right(safeId))

        when(mockRegistrationConnector.withOrganisationNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendBusinessRegistration(OrgName, address, contactDetails)

        result.futureValue mustBe Right(safeId)
      }

      "must return an error when when safeId can't be recovered" in {

        val response: Future[Left[ApiError, SafeId]] = Future.successful(Left(NotFoundError))

        when(mockRegistrationConnector.withOrganisationNoId(any())(any(), any())).thenReturn(response)

        val result: Future[Either[ApiError, SafeId]] = service.sendBusinessRegistration(OrgName, address, contactDetails)

        result.futureValue mustBe Left(NotFoundError)
      }
    }
  }
}
