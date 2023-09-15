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
import models.ReporterType.LimitedCompany
import models.{NormalMode, ReporterType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.{BusinessNamePage, ReporterTypePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.BusinessNameView

import scala.concurrent.Future

class BusinessNameControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.BusinessNameController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.BusinessNameController.onSubmit(NormalMode).url

  val selectedReporterTypeText = "llp"
  private def form             = new forms.BusinessNameFormProvider().apply(selectedReporterTypeText)

  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(ReporterTypePage, ReporterType.LimitedCompany).success.value

  "BusinessName Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReporterTypePage, LimitedCompany).success.value
      retrieveUserAnswersData(userAnswers)
      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "llp").toString
      }
    }

    "redirect to 'There is a problem with this page' page when business type is 'Sole trader'" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReporterTypePage, ReporterType.Sole).success.value

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ThereIsAProblemController.onPageLoad().url
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(userAnswers)

      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReporterTypePage, ReporterType.LimitedCompany).success.value.set(BusinessNamePage, "answer").success.value

      retrieveUserAnswersData(userAnswers)
      val application = guiceApplicationBuilder().build()
      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val view       = application.injector.instanceOf[BusinessNameView]
        val filledForm = form.bind(Map("value" -> "answer"))
        val result     = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(filledForm, NormalMode, "llp").toString

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        val request   = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[BusinessNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "llp")(request, messages).toString()
      }
    }

    "must redirect to 'SomeInformationIsMissing' when data is missing" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SomeInformationIsMissingController
        .onPageLoad()
        .url
    }

    "must redirect to 'There is a problem with this page' when business type is 'sole trader' on submission" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReporterTypePage, ReporterType.Sole).success.value

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ThereIsAProblemController
        .onPageLoad()
        .url
    }
  }
}
