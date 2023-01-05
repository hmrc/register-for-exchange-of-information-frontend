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
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.{ContactNamePage, ContactPhonePage}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ContactPhoneView

import scala.concurrent.Future

class ContactPhoneControllerSpec extends ControllerSpecBase {

  lazy val loadRoute: String   = routes.ContactPhoneController.onPageLoad(NormalMode).url
  lazy val submitRoute: String = routes.ContactPhoneController.onSubmit(NormalMode).url

  private def form = new forms.ContactPhoneFormProvider().apply()

  val contactName              = "Contact name"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(ContactNamePage, contactName).success.value

  "ContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, contactName, NormalMode).toString()
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers2 = userAnswers.set(ContactPhonePage, "01234 5678").success.value

      retrieveUserAnswersData(userAnswers2)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("01234 5678"), contactName, NormalMode).toString()
      }

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

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm                                                 = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = application.injector.instanceOf[ContactPhoneView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, contactName, NormalMode).toString()
      }
    }
  }
}
