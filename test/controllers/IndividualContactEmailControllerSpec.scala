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
import pages.IndividualContactEmailPage
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.IndividualContactEmailView

import scala.concurrent.Future

class IndividualContactEmailControllerSpec extends ControllerSpecBase {

  lazy val loadRoute: String   = routes.IndividualContactEmailController.onPageLoad(NormalMode).url
  lazy val submitRoute: String = routes.IndividualContactEmailController.onSubmit(NormalMode).url

  private def form = new forms.IndividualContactEmailFormProvider().apply()

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(IndividualContactEmailPage, TestEmail)
    .success
    .value

  "IndividualContactEmail Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[IndividualContactEmailView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, NormalMode).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      retrieveUserAnswersData(userAnswers)

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[IndividualContactEmailView]

      val filledForm = form.bind(
        Map(
          "email" -> TestEmail
        )
      )

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(filledForm, NormalMode).toString()

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("email", TestEmail))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm                                                 = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = app.injector.instanceOf[IndividualContactEmailView]

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode).toString()

    }
  }
}
