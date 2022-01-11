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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import models.MDR
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class BusinessAlreadyRegisteredControllerSpec extends SpecBase with ControllerMockFixtures {

  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  "BusinessAlreadyRegistered Controller" - {

    "return OK and the correct view for a GET with UTR" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(GET, routes.BusinessAlreadyRegisteredController.onPageLoadWithID(MDR).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "regime"       -> "MDR",
        "withID"       -> true,
        "emailAddress" -> frontendAppConfig.emailEnquiries
      )

      templateCaptor.getValue mustEqual "businessAlreadyRegistered.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "return OK and the correct view for a GET without UTR" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(GET, routes.BusinessAlreadyRegisteredController.onPageLoadWithoutID(MDR).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result     = route(app, request).value
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "regime"       -> "MDR",
        "withID"       -> false,
        "emailAddress" -> frontendAppConfig.emailEnquiries,
        "loginGG"      -> frontendAppConfig.loginUrl
      )

      templateCaptor.getValue mustEqual "businessAlreadyRegistered.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
