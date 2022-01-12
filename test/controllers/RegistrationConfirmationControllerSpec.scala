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
import models.{MDR, SubscriptionID, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.SubscriptionIDPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class RegistrationConfirmationControllerSpec extends SpecBase with ControllerMockFixtures {

  "RegistrationConfirmation Controller" - {

    "return OK and the correct view for a GET" in {
      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = UserAnswers(userAnswersId)
        .set(SubscriptionIDPage, SubscriptionID("SID"))
        .success
        .value

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad(MDR).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "registrationConfirmation.njk"
    }

    "render 'Technical Difficulties' page when Subscription Id is missing" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad(MDR).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "thereIsAProblem.njk"
    }
  }
}
