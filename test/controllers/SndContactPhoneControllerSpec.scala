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

package controllers

import base.ControllerSpecBase
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.{SndContactNamePage, SndContactPhonePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.SndContactPhoneView

import scala.concurrent.Future

class SndContactPhoneControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.SndContactPhoneController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.SndContactPhoneController.onSubmit(NormalMode).url
  val contactName      = "SecondContactName"
  val userAnswers      = UserAnswers(userAnswersId).set(SndContactNamePage, contactName).success.value

  private def form = new forms.SndContactPhoneFormProvider().apply()

  "SndContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)

      implicit val request = FakeRequest(GET, loadRoute)
      val result           = route(app, request).value

      val view = app.injector.instanceOf[SndContactPhoneView]
      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, contactName, NormalMode).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers2 = UserAnswers(userAnswersId)
        .set(SndContactNamePage, contactName)
        .success
        .value
        .set(SndContactPhonePage, "01234 5678")
        .success
        .value

      retrieveUserAnswersData(userAnswers2)

      implicit val request = FakeRequest(GET, loadRoute)

      val result = route(app, request).value
      val view   = app.injector.instanceOf[SndContactPhoneView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form.fill("01234 5678"), contactName, NormalMode).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(userAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "01234 5678"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)
      implicit val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm        = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = app.injector.instanceOf[SndContactPhoneView]

      contentAsString(result) mustEqual view(boundForm, contactName, NormalMode).toString()
    }

    "must redirect to 'SomeInformationIsMissing' when data is missing" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SomeInformationIsMissingController
        .onPageLoad()
        .url
    }
  }
}
