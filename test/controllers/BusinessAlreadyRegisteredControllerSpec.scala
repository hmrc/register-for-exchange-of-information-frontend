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
import config.FrontendAppConfig
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.BusinessAlreadyRegisteredView
import views.html.helper.form

import scala.concurrent.Future

class BusinessAlreadyRegisteredControllerSpec extends SpecBase with ControllerMockFixtures {

  "BusinessAlreadyRegistered Controller" - {

    "return OK and the correct view for a GET with UTR" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, routes.BusinessAlreadyRegisteredController.onPageLoadWithId().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAlreadyRegisteredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(withId = true)
      }
    }

    "return OK and the correct view for a GET without UTR" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, routes.BusinessAlreadyRegisteredController.onPageLoadWithoutId().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAlreadyRegisteredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(withId = false).toString
      }
    }
  }
}
