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

package handlers

import base.{ControllerMockFixtures, SpecBase}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}

class ErrorHandlerSpec extends SpecBase with ControllerMockFixtures {

  private val errorHandler = app.injector.instanceOf[ErrorHandler]

  "ErrorHandlerSpec" - {

    "handle onClientError" in {
      val fakeRequest = FakeRequest("GET", "/foo/mdr/test")

      val result =
        errorHandler.onClientError(fakeRequest, BAD_REQUEST, "The message")

      status(result) mustBe BAD_REQUEST
    }

    "handle onServerError" in {
      val fakeRequest = FakeRequest("GET", "/foo/mdr/test")

      val result =
        errorHandler.onServerError(fakeRequest, new IllegalArgumentException("the error"))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
