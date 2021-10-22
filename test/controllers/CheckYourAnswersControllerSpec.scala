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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, UnableToCreateEMTPSubscriptionError}
import models.subscription.response.SubscriptionID
import models.{MDR, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{SubscriptionService, TaxEnrolmentService}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  lazy val loadRoute   = routes.CheckYourAnswersController.onPageLoad(MDR).url
  lazy val submitRoute = routes.CheckYourAnswersController.onSubmit(MDR).url

  // first contact
  val firstContactName    = "first-contact-name"
  val firstContactEmail   = "first-contact-email"
  val isFirstContactPhone = true
  val firstContactPhone   = "+44 0808 157 0192"

  // second contact
  val isSecondContact                               = true
  val secondContactName                             = "second-contact-name"
  val secondContactEmail                            = "second-contact-email"
  val isSecondContactPhone                          = true
  val secondContactPhone                            = "+44 0808 157 0193"
  val mockSubscriptionService: SubscriptionService  = mock[SubscriptionService]
  val mockTaxEnrolmentsService: TaxEnrolmentService = mock[TaxEnrolmentService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
      .overrides(bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService))

  override def beforeEach: Unit = {
    reset(mockSubscriptionService)
    reset(mockTaxEnrolmentsService)
    super.beforeEach
  }

  "CheckYourAnswers Controller" - {
    "onPageLoad" - {
      "must return OK and the correct view for a GET - First Contact with phone" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(MDR).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json                = jsonCaptor.getValue
        val firstContactDetails = (json \ "firstContactList").toString

        templateCaptor.getValue mustEqual "checkYourAnswers.njk"
        firstContactDetails.contains("Contact name") mustBe true
        firstContactDetails.contains("Email address") mustBe true
        firstContactDetails.contains("Telephone number") mustBe isFirstContactPhone
      }

      "must return OK and the correct view for a GET - First Contact without phone" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(MDR).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json                = jsonCaptor.getValue
        val firstContactDetails = (json \ "firstContactList").toString

        templateCaptor.getValue mustEqual "checkYourAnswers.njk"
        firstContactDetails.contains("Contact name") mustBe true
        firstContactDetails.contains("Email address") mustBe true
        firstContactDetails.contains("Telephone number") mustBe isFirstContactPhone
        ((json \ "firstContactList")(2) \ "value" \ "text").get.as[String] mustEqual "None"
      }

      "must return OK and the correct view for a GET - Without Second Contact" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value
          .set(SecondContactPage, !isSecondContact)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(MDR).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json                 = jsonCaptor.getValue
        val firstContactDetails  = (json \ "firstContactList").toString
        val secondContactDetails = (json \ "secondContactList").toString

        templateCaptor.getValue mustEqual "checkYourAnswers.njk"
        firstContactDetails.contains("Contact name") mustBe true
        firstContactDetails.contains("Email address") mustBe true
        firstContactDetails.contains("Telephone number") mustBe isFirstContactPhone
        secondContactDetails.contains("Second contact name") mustBe !isSecondContact
      }

      "must return OK and the correct view for a GET - With Second Contact with phone" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value
          .set(SecondContactPage, isSecondContact)
          .success
          .value
          .set(SndContactNamePage, secondContactName)
          .success
          .value
          .set(SndContactEmailPage, secondContactEmail)
          .success
          .value
          .set(SndContactPhonePage, secondContactPhone)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(MDR).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json                 = jsonCaptor.getValue
        val firstContactDetails  = (json \ "firstContactList").toString
        val secondContactDetails = (json \ "secondContactList").toString

        templateCaptor.getValue mustEqual "checkYourAnswers.njk"
        firstContactDetails.contains("Contact name") mustBe true
        firstContactDetails.contains("Email address") mustBe true
        firstContactDetails.contains("Telephone number") mustBe isFirstContactPhone
        secondContactDetails.contains("Second contact name") mustBe true
        secondContactDetails.contains("Second contact email address") mustBe true
        secondContactDetails.contains("Second contact telephone number") mustBe isSecondContactPhone
      }

      "must return OK and the correct view for a GET - With Second Contact without phone" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value
          .set(SecondContactPage, isSecondContact)
          .success
          .value
          .set(SndContactNamePage, secondContactName)
          .success
          .value
          .set(SndContactEmailPage, secondContactEmail)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(MDR).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json                 = jsonCaptor.getValue
        val firstContactDetails  = (json \ "firstContactList").toString
        val secondContactDetails = (json \ "secondContactList").toString

        templateCaptor.getValue mustEqual "checkYourAnswers.njk"
        firstContactDetails.contains("Contact name") mustBe true
        firstContactDetails.contains("Email address") mustBe true
        firstContactDetails.contains("Telephone number") mustBe isFirstContactPhone
        secondContactDetails.contains("Second contact name") mustBe true
        secondContactDetails.contains("Second contact email address") mustBe true
        secondContactDetails.contains("Second contact telephone number") mustBe isSecondContactPhone
        ((json \ "secondContactList")(3) \ "value" \ "text").get.as[String] mustEqual "None"
      }
    }

    "onSubmit" - {
      "must redirect to 'confirmation' page for 'Individual with Id' journey" in {
        when(mockTaxEnrolmentsService.createEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual NOT_IMPLEMENTED

      }

      "must redirect to 'confirmation' page for 'Business with Id' journey" in {
        when(mockTaxEnrolmentsService.createEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual NOT_IMPLEMENTED

      }

      "must redirect to 'badRequest' page for 'Business with Id' journey when tax enrolment fails" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))
        when(mockTaxEnrolmentsService.createEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Left(BAD_REQUEST)))
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
        templateCaptor.getValue mustEqual "badRequest.njk"

      }

      "must redirect to 'confirmation' page for 'Individual without Id' journey" in {
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, false)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual NOT_IMPLEMENTED

      }

      "must redirect to 'confirmation' page for 'Business without Id' journey" in {
        when(mockTaxEnrolmentsService.createEnrolment(any(), any())(any(), any())).thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, false)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual NOT_IMPLEMENTED

      }

      "must redirect to 'JourneyRecovery' page when some information missing" in {
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID(""))))
        val userAnswers = UserAnswers("Id")

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(MDR).url
      }

      "must redirect to 'Duplication submission' page when there is duplication submission" in {
        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Left(DuplicateSubmissionError)))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual NOT_IMPLEMENTED

      }

      "must render 'badRequest' page when 'createSubscription' fails with BadRequestError" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Left(BadRequestError)))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
        templateCaptor.getValue mustEqual "badRequest.njk"

      }

      "must render 'thereIsAProblem' page when 'createSubscription' fails with UnableToCreateEMTPSubscriptionError" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        when(mockSubscriptionService.createSubscription(any())(any(), any())).thenReturn(Future.successful(Left(UnableToCreateEMTPSubscriptionError)))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
        templateCaptor.getValue mustEqual "thereIsAProblem.njk"

      }
    }
  }
}
