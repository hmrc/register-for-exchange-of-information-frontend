/*
 * Copyright 2021 HM Revenue & Customs
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
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class SignedOutControllerSpec extends SpecBase with ControllerMockFixtures {

  private def signOutRoute: String = controllers.auth.routes.SignedOutController.signOut().url

  "SignOut Controller" - {

    "redirect to feedback survey page" in {

      val appConfig = app.injector.instanceOf[FrontendAppConfig]
      val result    = route(app, FakeRequest(GET, signOutRoute)).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(appConfig.exitSurveyUrl)

    }
  }
}
