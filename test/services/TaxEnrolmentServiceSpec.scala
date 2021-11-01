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
import connectors.TaxEnrolmentsConnector
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import cats.implicits.catsStdInstancesForFuture
import models.{Address, Country, NonUkName, UserAnswers}
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.error.ApiError
import models.error.ApiError.{SubscriptionCreationError, UnableToCreateEnrolmentError}
import models.matching.MatchingInfo
import models.subscription.response.SubscriptionID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.ArgumentMatchers.any
import pages.{
  AddressWithoutIdPage,
  ContactEmailPage,
  DoYouHaveNINPage,
  DoYouHaveUniqueTaxPayerReferencePage,
  MatchingInfoPage,
  NonUkNamePage,
  WhatAreYouRegisteringAsPage
}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}

class TaxEnrolmentServiceSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  val mockTaxEnrolmentsConnector = mock[TaxEnrolmentsConnector]

  val service: TaxEnrolmentService = app.injector.instanceOf[TaxEnrolmentService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[TaxEnrolmentsConnector].toInstance(mockTaxEnrolmentsConnector))

  override def beforeEach: Unit = {
    reset(mockTaxEnrolmentsConnector)
    super.beforeEach
  }

  "TaxEnrolmentService" - {
    "must create a subscriptionModel from userAnswers and call the taxEnrolmentsConnector returning with a Successful NO_CONTENT" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Right(NO_CONTENT))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)

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
        .set(MatchingInfoPage, MatchingInfo("safeId", None, None))
        .success
        .value

      val result = service.createEnrolment(userAnswers, subscriptionID)

      result.futureValue mustBe Right(NO_CONTENT)
    }

    "must return BAD_REQUEST when 400 is received from taxEnrolments" in {

      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Left(UnableToCreateEnrolmentError))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)

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
        .set(MatchingInfoPage, MatchingInfo("safeId", None, None))
        .success
        .value

      val result = service.createEnrolment(userAnswers, subscriptionID)

      result.futureValue mustBe Left(UnableToCreateEnrolmentError)
    }

    "must return INTERNAL_SERVER_ERROR when MatchingInfo is missing from userAnswers" in {
      val response: EitherT[Future, ApiError, Int] = EitherT.fromEither[Future](Right(NO_CONTENT))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(response)

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

      val result = service.createEnrolment(userAnswers, subscriptionID)

      result.futureValue mustBe Left(SubscriptionCreationError)
    }
  }

}
