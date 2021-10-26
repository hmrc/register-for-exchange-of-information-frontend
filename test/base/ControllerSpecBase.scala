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

package base

import controllers.routes
import play.api.test.{FakeRequest, Helpers}

trait ControllerSpecBase extends SpecBase with ControllerMockFixtures {

  val loadRoute: String
  val submitRoute: String

  "must redirect for a POST if no existing data is found" in {

    import Helpers._

    retrieveNoData()

    val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value[0]", "value"))

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoadDef().url
  }

  "must redirect for a GET if no existing data is found" in {

    import Helpers._

    retrieveNoData()

    val request = FakeRequest(GET, loadRoute)

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoadDef().url
  }

}
