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
import pages.{SndConHavePhonePage, SndContactNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SndConHavePhoneView

import scala.concurrent.Future

class SndConHavePhoneControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.SndConHavePhoneController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.SndConHavePhoneController.onSubmit(NormalMode).url

  private def form = new forms.SndConHavePhoneFormProvider().apply()

  val userAnswers = emptyUserAnswers.set(SndContactNamePage, name.fullName).success.value

  "SndConHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, loadRoute)

      val view = app.injector.instanceOf[SndConHavePhoneView]

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, name.fullName, NormalMode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers2 = emptyUserAnswers
        .set(SndContactNamePage, name.fullName)
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
      retrieveUserAnswersData(userAnswers2)
      val request      = FakeRequest(GET, loadRoute)

      val view = app.injector.instanceOf[SndConHavePhoneView]

      val result = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> "true"))
      contentAsString(result) mustEqual view(filledForm, name.fullName, NormalMode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(SndContactNamePage, name.fullName).success.value

      retrieveUserAnswersData(userAnswers)
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

      val view = app.injector.instanceOf[SndConHavePhoneView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, name.fullName, NormalMode)(request, messages).toString

    }
  }
}
