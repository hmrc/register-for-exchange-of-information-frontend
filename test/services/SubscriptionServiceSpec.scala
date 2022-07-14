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
import connectors.SubscriptionConnector
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.error.ApiError
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, MandatoryInformationMissingError, NotFoundError, UnableToCreateEMTPSubscriptionError}
import models.{Address, Country, NonUkName, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends SpecBase with MockServiceApp with MockitoSugar with ScalaCheckPropertyChecks {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  val mockAuditService: AuditService                   = mock[AuditService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[AuditService].toInstance(mockAuditService)
    )

  val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]

  override def beforeEach: Unit = {
    reset(mockSubscriptionConnector, mockAuditService)
    super.beforeEach()
  }

  "SubscriptionService" - {
    "must return 'SubscriptionID' on creating subscription" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)
      when(mockAuditService.sendAuditEvent(any(), any())(any())).thenReturn(Future.successful(Success))

      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
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
        .set(IndividualContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value
        .set(IndividualAddressWithoutIdPage, address)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val result = service.checkAndCreateSubscription(safeId, userAnswers)
      result.futureValue mustBe Right(SubscriptionID("id"))

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' on creating subscription Have second contact is true" in {
      val subscriptionID                                      = SubscriptionID("id")
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Right(subscriptionID))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)
      when(mockAuditService.sendAuditEvent(any(), any())(any())).thenReturn(Future.successful(Success))

      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
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
        .set(IndividualContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value
        .set(IndividualAddressWithoutIdPage, address)
        .success
        .value
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndConHavePhonePage, false)
        .success
        .value

      val result = service.checkAndCreateSubscription(safeId, userAnswers)
      result.futureValue mustBe Right(SubscriptionID("id"))

      verify(mockSubscriptionConnector, times(1)).readSubscription(any())(any(), any())
      verify(mockSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
    }

    "must return 'SubscriptionID' when there is already a subscription exists" in {
      val subscriptionID = SubscriptionID("id")

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(subscriptionID)))

      val result = service.checkAndCreateSubscription(safeId, emptyUserAnswers)
      result.futureValue mustBe Right(subscriptionID)
    }

    "must return MandatoryInformationMissingError when UserAnswers is empty" in {
      val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(MandatoryInformationMissingError()))

      when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

      val result = service.checkAndCreateSubscription(safeId, UserAnswers("id"))

      result.futureValue mustBe Left(MandatoryInformationMissingError())
    }

    "must return error when it fails to create subscription" in {
      val errors = Seq(NotFoundError, BadRequestError, DuplicateSubmissionError, UnableToCreateEMTPSubscriptionError)
      for (error <- errors) {
        val userAnswers = UserAnswers("id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactEmailPage, "test@test.com")
          .success
          .value
          .set(ContactNamePage, "Name Name")
          .success
          .value
          .set(IsContactTelephonePage, false)
          .success
          .value
          .set(SecondContactPage, false)
          .success
          .value

        val response: EitherT[Future, ApiError, SubscriptionID] = EitherT.fromEither[Future](Left(error))
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(response)

        val result = service.checkAndCreateSubscription(safeId, userAnswers)

        result.futureValue mustBe Left(error)
      }
    }

    "getDisplaySubscriptionId" - {

      "must return 'SubscriptionID' for valid input" in {
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
        val result = service.getDisplaySubscriptionId(safeId)
        result.futureValue mustBe Some(SubscriptionID("id"))
      }

      "must return 'None' for any failures of exceptions" in {
        when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(None))
        val result = service.getDisplaySubscriptionId(safeId)
        result.futureValue mustBe None
      }
    }
  }

}
