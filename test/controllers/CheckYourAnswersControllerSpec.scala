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
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages._
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ControllerMockFixtures {

  lazy val loadRoute   = routes.CheckYourAnswersController.onPageLoad().url
  lazy val submitRoute = routes.CheckYourAnswersController.onPageLoad().url // todo del
  //lazy val submitRoute = routes.CheckYourAnswersController.onSubmit().url // todo once submit is implemented

  // first contact
  val firstContactName    = "first-contact-name"
  val firstContactEmail   = "first-contact-email"
  val isFirstContactPhone = true
  val firstContactPhone   = "+44 0808 157 0192"

  // second contact
  val isSecondContact      = true
  val secondContactName    = "second-contact-name"
  val secondContactEmail   = "second-contact-email"
  val isSecondContactPhone = true
  val secondContactPhone   = "+44 0808 157 0193"

  "CheckYourAnswers Controller" - {

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

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

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

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

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

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

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

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

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

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

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
}
