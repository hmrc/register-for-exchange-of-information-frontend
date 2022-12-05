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
import pages.IndividualContactPhonePage
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.IndividualContactPhoneView

import scala.concurrent.Future

class IndividualContactPhoneControllerSpec extends ControllerSpecBase {

  lazy val loadRoute: String   = routes.IndividualContactPhoneController.onPageLoad(NormalMode).url
  lazy val submitRoute: String = routes.IndividualContactPhoneController.onSubmit(NormalMode).url

  private def form: Form[String] = new forms.IndividualContactPhoneFormProvider().apply()

  "IndividualContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[IndividualContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString()

      }

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndividualContactPhonePage, "07500000000").success.value

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = application.injector.instanceOf[IndividualContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("07500000000"), NormalMode).toString()

      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "07500000000"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm                                                 = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = application.injector.instanceOf[IndividualContactPhoneView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode).toString()
      }

    }
  }
}
