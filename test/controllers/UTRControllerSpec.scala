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
import models.{NormalMode, ReporterType}
import org.mockito.ArgumentMatchers.any
import pages.{ReporterTypePage, UTRPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UTRView

import scala.concurrent.Future

class UTRControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.UTRController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.UTRController.onSubmit(NormalMode).url

  private def form = new forms.UTRFormProvider().apply("Self Assessment") // has to match ReporterType in user answer

  val userAnswers = emptyUserAnswers.set(ReporterTypePage, ReporterType.Sole).success.value
  val taxType     = "Self Assessment"

  "UTR Controller" - {

    "must return OK and the correct view for a GET when self assessment" in {

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, loadRoute)
      val view    = app.injector.instanceOf[UTRView]

      val result = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, NormalMode, taxType)(request, messages).toString
    }

    "must return OK and the correct view for a GET when corporation tax" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, ReporterType.LimitedCompany).success.value

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, loadRoute)
      val view    = app.injector.instanceOf[UTRView]
      val taxType = "Corporation Tax"

      val result = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, NormalMode, taxType)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Sole)
          .success
          .value
          .set(UTRPage, utr)
          .success
          .value

      retrieveUserAnswersData(userAnswers)
      val request   = FakeRequest(GET, loadRoute)
      val view      = app.injector.instanceOf[UTRView]
      val boundForm = form.bind(Map("value" -> utr.uniqueTaxPayerReference))

      val result = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(boundForm, NormalMode, taxType)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(userAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", utr.uniqueTaxPayerReference))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)
      val request   = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
      val view      = app.injector.instanceOf[UTRView]
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode, taxType)(request, messages).toString
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
  }

}
