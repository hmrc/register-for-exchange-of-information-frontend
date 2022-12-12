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

import base.{ControllerMockFixtures, SpecBase}
import models.{SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.SubscriptionIDPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{RegistrationConfirmationView, ThereIsAProblemView}

import scala.concurrent.Future

class RegistrationConfirmationControllerSpec extends SpecBase with ControllerMockFixtures {

  "RegistrationConfirmation Controller" - {

    "return OK and the correct view for a GET" in {
      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers(userAnswersId)
        .set(SubscriptionIDPage, SubscriptionID("SID"))
        .success
        .value

      retrieveUserAnswersData(userAnswers)
      val request = FakeRequest(GET, controllers.routes.RegistrationConfirmationController.onPageLoad().url)

      val view = app.injector.instanceOf[RegistrationConfirmationView]

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view("SID")(request, messages).toString
    }

    "render 'Technical Difficulties' page when Subscription Id is missing" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, controllers.routes.RegistrationConfirmationController.onPageLoad().url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }
  }
}
