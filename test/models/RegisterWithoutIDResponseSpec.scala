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

package models

import base.SpecBase
import helpers.JsonFixtures.{registerWithoutIDResponse, registerWithoutIDResponseJson}
import models.matching.SafeId
import models.register.response.{RegisterWithoutIDResponse, RegisterWithoutIDResponseDetail, RegistrationWithoutIDResponse}
import models.shared.ResponseCommon
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class RegisterWithoutIDResponseSpec extends SpecBase with ScalaCheckPropertyChecks {

  val responseCommon: ResponseCommon =
    ResponseCommon(status = "OK", statusText = Some("Success"), processingDate = "2020-09-01T01:00:00Z", returnParameters = None)
  val responseDetail: RegisterWithoutIDResponseDetail = RegisterWithoutIDResponseDetail(SAFEID = SafeId("XE0000123456789"), ARN = None)

  val registerWithoutID: RegistrationWithoutIDResponse = RegistrationWithoutIDResponse(
    RegisterWithoutIDResponse(responseCommon, Some(responseDetail))
  )

  "PayloadRegistrationWithoutIDResponse" - {

    "must serialise PayloadRegistrationWithoutIDResponse" in {
      Json.toJson(registerWithoutID) mustBe registerWithoutIDResponseJson
    }

    "must deserialise PayloadRegistrationWithoutIDResponse" in {
      Json.parse(registerWithoutIDResponse).validate[RegistrationWithoutIDResponse].get mustBe registerWithoutID
    }
  }

}
