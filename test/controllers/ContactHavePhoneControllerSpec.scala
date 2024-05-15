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
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import pages.{ContactHavePhonePage, ContactNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ContactHavePhoneView

import scala.concurrent.Future

class ContactHavePhoneControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.ContactHavePhoneController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.ContactHavePhoneController.onSubmit(NormalMode).url

  private def form = new forms.ContactHavePhoneFormProvider().apply()

  val userAnswers = emptyUserAnswers.set(ContactNamePage, name.fullName).success.value

  "ContactHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ContactHavePhoneView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, name.fullName, NormalMode)(request, messages).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers2 = emptyUserAnswers
        .set(ContactNamePage, name.fullName)
        .success
        .value
        .set(ContactHavePhonePage, true)
        .success
        .value
      retrieveUserAnswersData(userAnswers2)
      val request      = FakeRequest(GET, loadRoute)

      val view       = app.injector.instanceOf[ContactHavePhoneView]
      val filledForm = form.bind(Map("value" -> "true"))

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(filledForm, name.fullName, NormalMode)(request, messages).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)
      val request   = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))
      val view      = app.injector.instanceOf[ContactHavePhoneView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, name.fullName, NormalMode)(request, messages).toString
    }
  }
}
