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
import matchers.JsonMatchers
import models.MDR
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class JourneyRecoveryControllerSpec extends SpecBase with ControllerMockFixtures with NunjucksSupport with JsonMatchers {

  "JourneyRecovery Controller" - {

    "when a relative continue Url is supplied" - {

      "must return OK and the continue view" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val continueUrl = RedirectUrl("/foo")
        retrieveUserAnswersData(emptyUserAnswers)
        val request        = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(MDR, Some(continueUrl)).url)
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val result = route(app, request).value

        status(result) mustEqual OK

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val expectedJson = Json.obj("continueUrl" -> "/foo")

        templateCaptor.getValue mustEqual "journeyRecoveryContinue.njk"
        jsonCaptor.getValue must containJson(expectedJson)
      }
    }

    "when an absolute continue Url is supplied" - {

      "must return OK and the start again view" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val continueUrl = RedirectUrl("https://foo.com")
        retrieveUserAnswersData(emptyUserAnswers)
        val request        = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(MDR, Some(continueUrl)).url)
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])

        val result = route(app, request).value

        status(result) mustEqual OK

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        templateCaptor.getValue mustEqual "journeyRecoveryStartAgain.njk"
      }
    }

    "when no continue Url is supplied" - {

      "must return OK and the start again view" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        retrieveUserAnswersData(emptyUserAnswers)
        val request        = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(MDR).url)
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])

        val result = route(app, request).value

        status(result) mustEqual OK

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        templateCaptor.getValue mustEqual "journeyRecoveryStartAgain.njk"
      }
    }
  }
}
