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

import base.{ControllerMockFixtures, SpecBase}
import matchers.JsonMatchers
import org.mockito.ArgumentMatchers.any
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with ControllerMockFixtures with JsonMatchers {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" in {

        when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

        retrieveUserAnswersData(emptyUserAnswers)

        val request = FakeRequest(GET, routes.KeepAliveController.keepAlive().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        verify(mockSessionRepository, times(1)).keepAlive(emptyUserAnswers.id)
      }
    }

    "when the user has not answered any questions" - {

      "must return OK" in {

        when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

        retrieveNoData()

        val request = FakeRequest(GET, routes.KeepAliveController.keepAlive().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        verify(mockSessionRepository, never).keepAlive(any())
      }
    }
  }
}
