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
import forms.BusinessWithoutIDNameFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.BusinessWithoutIDNamePage
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.BusinessWithoutIDNameView

import scala.concurrent.Future

class BusinessWithoutIDNameControllerSpec extends ControllerSpecBase {

  lazy val loadRoute: String   = routes.BusinessWithoutIDNameController.onPageLoad(NormalMode).url
  lazy val submitRoute: String = routes.BusinessWithoutIDNameController.onSubmit(NormalMode).url

  val formProvider = new BusinessWithoutIDNameFormProvider()
  val form         = formProvider()

  "BusinessWithoutIDName Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessWithoutIDNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(BusinessWithoutIDNamePage, "answer").success.value
      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder()
        .build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val view = application.injector.instanceOf[BusinessWithoutIDNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode).toString

      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .build()

      running(application) {
        val request =
          FakeRequest(POST, loadRoute)
            .withFormUrlEncodedBody(("businessWithoutIDName", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("businessWithoutIDName", ""))

        val boundForm = form.bind(Map("businessWithoutIDName" -> ""))

        val view = application.injector.instanceOf[BusinessWithoutIDNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode).toString
      }
    }
  }
}
