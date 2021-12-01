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

import base.{ControllerMockFixtures, SpecBase}
import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, UnableToCreateEnrolmentError}
import models.matching.MatchingType.AsIndividual
import models.matching.RegistrationInfo
import models.{Address, Country, MDR, NonUkName, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.http.Status.NO_CONTENT
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxEnrolmentServiceSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  val mockTaxEnrolmentsConnector       = mock[TaxEnrolmentsConnector]
  val mockEnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]

  val service: TaxEnrolmentService = app.injector.instanceOf[TaxEnrolmentService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[TaxEnrolmentsConnector].toInstance(mockTaxEnrolmentsConnector))
      .overrides(bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector))

  override def beforeEach: Unit = {
    reset(mockTaxEnrolmentsConnector, mockEnrolmentStoreProxyConnector)
    super.beforeEach
  }

  "TaxEnrolmentService" - {
    "must create a subscriptionModel from userAnswers and call the taxEnrolmentsConnector returning with a Successful NO_CONTENT" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Right(NO_CONTENT))

      when(mockTaxEnrolmentsConnector.createEnrolment(any(), any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any(), any())(any(), any())).thenReturn(EitherT.fromEither[Future](Right(Unit)))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("a", "b"))
        .success
        .value
        .set(ContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(AddressWithoutIdPage, address)
        .success
        .value
        .set(RegistrationInfoPage, RegistrationInfo.build("safeId", AsIndividual))
        .success
        .value

      val result = service.checkAndCreateEnrolment("safeId", userAnswers, subscriptionID, MDR)

      result.futureValue mustBe Right(NO_CONTENT)
    }

    "must return BAD_REQUEST when 400 is received from taxEnrolments" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Left(UnableToCreateEnrolmentError))

      when(mockTaxEnrolmentsConnector.createEnrolment(any(), any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any(), any())(any(), any())).thenReturn(EitherT.fromEither[Future](Right(Unit)))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("a", "b"))
        .success
        .value
        .set(ContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(AddressWithoutIdPage, address)
        .success
        .value
        .set(RegistrationInfoPage, RegistrationInfo.build("safeId", AsIndividual))
        .success
        .value

      val result = service.checkAndCreateEnrolment("safeId", userAnswers, subscriptionID, MDR)

      result.futureValue mustBe Left(UnableToCreateEnrolmentError)
    }

    "must return EnrolmentExistsError when there is already an enrolment" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Left(UnableToCreateEnrolmentError))
      val groupIds                                 = GroupIds(Seq("groupId"), Seq.empty)

      when(mockTaxEnrolmentsConnector.createEnrolment(any(), any())(any(), any())).thenReturn(response)
      when(mockEnrolmentStoreProxyConnector.enrolmentStatus(any(), any())(any(), any()))
        .thenReturn(EitherT.fromEither[Future](Left(EnrolmentExistsError(groupIds))))

      val subscriptionID = SubscriptionID("id")
      val address        = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("a", "b"))
        .success
        .value
        .set(ContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(AddressWithoutIdPage, address)
        .success
        .value
        .set(RegistrationInfoPage, RegistrationInfo.build("safeId", AsIndividual))
        .success
        .value

      val result = service.checkAndCreateEnrolment("safeId", userAnswers, subscriptionID, MDR)

      result.futureValue mustBe Left(EnrolmentExistsError(groupIds))
    }
  }

}
