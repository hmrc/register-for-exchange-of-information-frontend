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

package controllers

import base.ControllerSpecBase
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.{ContactEmailPage, ContactNamePage, WhatAreYouRegisteringAsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ContactEmailView

import scala.concurrent.Future

class ContactEmailControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.ContactEmailController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.ContactEmailController.onSubmit(NormalMode).url

  private def form = new forms.ContactEmailFormProvider().apply()

  val contactName = "Name"

  val userAnswers = UserAnswers(userAnswersId)
    .set(ContactNamePage, contactName)
    .success
    .value
    .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
    .success
    .value

  "ContactEmail Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, contactName).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers2 =
        userAnswers
          .set(ContactEmailPage, "some@email.com")
          .success
          .value

      retrieveUserAnswersData(userAnswers2)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("some@email.com"), NormalMode, "Name").toString()
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "some@email.com"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm        = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactEmailView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "Name").toString()
      }
    }
  }
}
