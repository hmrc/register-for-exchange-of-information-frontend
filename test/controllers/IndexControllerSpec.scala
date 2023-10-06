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
import controllers.actions.{CtUtrRetrievalAction, FakeCtUtrRetrievalAction}
import matchers.JsonMatchers
import models.{NormalMode, UniqueTaxpayerReference}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

class IndexControllerSpec extends SpecBase with ControllerMockFixtures with JsonMatchers {

  override lazy val app: Application = guiceApplicationBuilder()
    .overrides(bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction))
    .build()

  "Index Controller" - {

    "must redirect to ReporterTypePage for a GET when there is no CT UTR" in {
      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ReporterTypeController.onPageLoad(NormalMode).url

    }

    "must redirect to IsThisYourBusinessPage for a GET when there is a CT UTR" in {
      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction(Option(UniqueTaxpayerReference("123"))))

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IsThisYourBusinessController.onPageLoad(NormalMode).url

    }
  }
}
