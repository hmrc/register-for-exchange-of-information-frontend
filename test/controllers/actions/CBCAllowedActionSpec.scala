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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.requests.IdentifierRequest
import models.{CBC, MDR}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CBCAllowedActionSpec extends SpecBase {

  val mockFrontend: FrontendAppConfig = mock[FrontendAppConfig]

  val response: IdentifierRequest[AnyContentAsEmpty.type] => Future[Result] = {
    _ =>
      Future.successful(Ok(HtmlFormat.empty))
  }

  "CBCAllowedAction" - {
    "Handle CBC regime" - {
      "must allow request for CBC if allow flag is true" in {
        when(mockFrontend.allowCBCregistration).thenReturn(true)

        lazy val action = new CBCAllowedActionWithRegime(CBC, mockFrontend)

        def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

        val idr: IdentifierRequest[AnyContentAsEmpty.type] = IdentifierRequest(fakeRequest, "id", AffinityGroup.Organisation)

        val testAction: Future[Result] = action.invokeBlock(idr, response)
        status(testAction) mustBe OK
      }
      "must redirect request for CBC if allow flag is false" in {
        when(mockFrontend.allowCBCregistration).thenReturn(false)

        lazy val action = new CBCAllowedActionWithRegime(CBC, mockFrontend)

        def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

        val idr: IdentifierRequest[AnyContentAsEmpty.type] = IdentifierRequest(fakeRequest, "id", AffinityGroup.Organisation)

        val testAction: Future[Result] = action.invokeBlock(idr, response)
        status(testAction) mustBe SEE_OTHER
      }
    }
    "Ignore MDR regime" - {
      "must allow request for MDR if allow cbc flag is false " in {
        when(mockFrontend.allowCBCregistration).thenReturn(false)

        lazy val action = new CBCAllowedActionWithRegime(MDR, mockFrontend)

        def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

        val idr: IdentifierRequest[AnyContentAsEmpty.type] = IdentifierRequest(fakeRequest, "id", AffinityGroup.Organisation)

        val testAction: Future[Result] = action.invokeBlock(idr, response)
        status(testAction) mustBe OK
      }
      "must allow request for MDR if allow cbc flag is true " in {
        when(mockFrontend.allowCBCregistration).thenReturn(true)

        lazy val action = new CBCAllowedActionWithRegime(MDR, mockFrontend)

        def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

        val idr: IdentifierRequest[AnyContentAsEmpty.type] = IdentifierRequest(fakeRequest, "id", AffinityGroup.Organisation)

        val testAction: Future[Result] = action.invokeBlock(idr, response)
        status(testAction) mustBe OK
      }
    }
  }

}
