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
import models.{Name, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.WhatIsYourNamePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.WhatIsYourNameView

import scala.concurrent.Future

class WhatIsYourNameControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.WhatIsYourNameController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.WhatIsYourNameController.onSubmit(NormalMode).url

  private def form = new forms.WhatIsYourNameFormProvider().apply()

  val firstName: String = "First Name"
  val lastName: String  = "Last"
  val validAnswer: Name = Name(firstName, lastName)

  val validData = Map(
    "firstName" -> firstName,
    "lastName"  -> lastName
  )

  "WhatIsYourName Controller" - {


    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      implicit val request = FakeRequest(GET, loadRoute)
      val result = route(app, request).value

      val view = app.injector.instanceOf[WhatIsYourNameView]
      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, NormalMode).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WhatIsYourNamePage, validAnswer).success.value
      retrieveUserAnswersData(userAnswers)
      implicit val request = FakeRequest(GET, loadRoute)

      val view = app.injector.instanceOf[WhatIsYourNameView]

      val result     = route(app, request).value
      val filledForm = form.bind(validData)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(filledForm, NormalMode).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("firstName", firstName), ("lastName", lastName))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = app.injector.instanceOf[WhatIsYourNameView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages).toString
    }
  }
}
