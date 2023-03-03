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
import pages.DateOfBirthWithoutIdPage
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DateOfBirthView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class DateOfBirthWithoutIdControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.DateOfBirthWithoutIdController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.DateOfBirthWithoutIdController.onSubmit(NormalMode).url

  private def form = new forms.DateOfBirthFormProvider().apply()

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, submitRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "WhatIsYourDateOfBirth Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[DateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString

      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(DateOfBirthWithoutIdPage, validAnswer).success.value
      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[DateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LocalDate.now()), NormalMode).toString()
      }

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val result = route(app, postRequest).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm        = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = application.injector.instanceOf[DateOfBirthView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode).toString()
      }
    }
  }
}
