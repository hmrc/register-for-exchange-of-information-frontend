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

package controllers.auth

import base.{ControllerMockFixtures, SpecBase}
import config.FrontendAppConfig
import matchers.JsonMatchers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with ControllerMockFixtures with JsonMatchers {

  "signOut" - {

    "must clear user answers and redirect to sign out, specifying the exit survey as the continue URL" in {

      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)
      retrieveNoData()
      val appConfig = app.injector.instanceOf[FrontendAppConfig]
      val request   = FakeRequest(GET, routes.AuthController.signOut().url)

      val result = route(app, request).value

      val expectedRedirectUrl = s"${appConfig.signOutUrl}"

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedRedirectUrl
      verify(mockSessionRepository, times(1)).clear(eqTo(UserAnswersId))
    }
  }

  "signOutNoSurvey" - {

    "must clear users answers and redirect to signed out page" in {

      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      retrieveNoData()
      val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey().url)

      val result = route(app, request).value

      val expectedRedirectUrl = routes.SignedOutController.onPageLoad().url

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedRedirectUrl
      verify(mockSessionRepository, times(1)).clear(eqTo(UserAnswersId))
    }
  }
}
