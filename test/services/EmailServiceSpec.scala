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

import base.SpecBase
import connectors.EmailConnector
import generators.Generators
import models.ReporterType.LimitedCompany
import models.{ReporterType, SubscriptionID}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class EmailServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  override def beforeEach(): Unit =
    reset(
      mockEmailConnector
    )

  val mockEmailConnector: EmailConnector = mock[EmailConnector]

  lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[EmailConnector].toInstance(mockEmailConnector)
    )
    .build()

  val emailService: EmailService = app.injector.instanceOf[EmailService]

  val subscriptionID = SubscriptionID("XADAC0000123456")

  "Email Service" - {
    "sendAnLogEmail" - {
      "must submit to the email connector with valid business details Organisation and return Some(Accepted)" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(ACCEPTED, ""))
          )

        val result = emailService.sendAnLogEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result mustBe ACCEPTED

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }
      "must submit to the email connector with valid business details Individual and return Some(Accepted)" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Individual)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(IndividualContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(ACCEPTED, ""))
          )

        val result = emailService.sendAnLogEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result mustBe ACCEPTED

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }
      "must Internal_Server_Error when both Contact and Individual emails pages are missing" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(ACCEPTED, ""))
          )

        val result = emailService.sendAnLogEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result mustBe INTERNAL_SERVER_ERROR

            verify(mockEmailConnector, times(0)).sendEmail(any())(any())
        }
      }
      "must submit to the email connector and return NOT_FOUND when the template is missing" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(NOT_FOUND, ""))
          )

        val result = emailService.sendAnLogEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result mustBe NOT_FOUND

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }
      "must submit to the email connector and return BAD_REQUEST email service rejects request" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(BAD_REQUEST, ""))
          )

        val result = emailService.sendAnLogEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result mustBe BAD_REQUEST

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }
    }
    "sendEmail" - {
      "must submit to the email connector when 1 set of business valid details provided" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe Some(OK)

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }

      "must submit to the email connector when sole trader valid details provided" in {
        val userAnswers = emptyUserAnswers
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ReporterTypePage, ReporterType.Sole)
          .success
          .value
          .set(SoleNamePage, name)
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe Some(OK)

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }

      "must submit to the email connector when 1 individuals set of valid details provided" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Individual)
          .success
          .value
          .set(WhatIsYourNamePage, name)
          .success
          .value
          .set(IndividualContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe Some(OK)

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }

      "must submit to the email connector when 1 nonUk individuals set of valid details provided" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Individual)
          .success
          .value
          .set(NonUkNamePage, nonUkName)
          .success
          .value
          .set(IndividualContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe Some(OK)

            verify(mockEmailConnector, times(1)).sendEmail(any())(any())
        }
      }

      "must submit to the email connector twice when 2 sets of valid details provided" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, TestEmail)
          .success
          .value
          .set(SndContactNamePage, "")
          .success
          .value
          .set(SndContactEmailPage, TestEmail)
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe Some(OK)

            verify(mockEmailConnector, times(2)).sendEmail(any())(any())
        }
      }

      "must fail to submit to the email connector when invalid email address provided" in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.LimitedCompany)
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(ContactNamePage, "")
          .success
          .value
          .set(ContactEmailPage, "test")
          .success
          .value

        when(mockEmailConnector.sendEmail(any())(any()))
          .thenReturn(
            Future.successful(HttpResponse(OK, ""))
          )

        val result = emailService.sendEmail(userAnswers, subscriptionID)

        whenReady(result) {
          result =>
            result.map(_.status) mustBe None
        }
      }
    }
  }
}
