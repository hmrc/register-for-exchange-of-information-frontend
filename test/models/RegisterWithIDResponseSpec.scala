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
import helpers.JsonFixtures._
import models.matching.SafeId
import models.register.response.details.{AddressResponse, IndividualResponse}
import models.register.response.{RegisterWithIDResponse, RegisterWithIDResponseDetail, RegistrationWithIDResponse}
import models.shared.{ContactDetails, Parameters, ResponseCommon}
import play.api.libs.json.Json

class RegisterWithIDResponseSpec extends SpecBase {

  val responseCommon: ResponseCommon = ResponseCommon(
    "OK",
    Some("Sample status text"),
    "2016-08-16T15:55:30Z",
    Some(Seq(Parameters("SAP_NUMBER", "0123456789")))
  )

  val responseDetail: RegisterWithIDResponseDetail = RegisterWithIDResponseDetail(
    SafeId("XE0000123456789"),
    Some("WARN8764123"),
    isEditable = true,
    isAnAgent = false,
    isAnIndividual = true,
    isAnASAgent = None,
    partnerDetails = IndividualResponse(
      "Ron",
      Some("Madisson"),
      "Burgundy",
      Some("1980-12-12")
    ),
    address = AddressResponse("100 Parliament Street", None, None, Some("London"), Some("SW1A 2BQ"), "GB"),
    contactDetails = ContactDetails(
      Some("1111111"),
      Some("2222222"),
      Some("1111111"),
      Some("test@test.org")
    )
  )

  val payloadModel: RegistrationWithIDResponse =
    RegistrationWithIDResponse(
      RegisterWithIDResponse(responseCommon, Some(responseDetail))
    )

  "RegisterWithIDResponse" - {
    "must marshall from json" in {
      Json.parse(withIDIndividualResponse).validate[RegistrationWithIDResponse].get mustBe payloadModel
    }

    "must serialise to json" in {
      Json.toJson(payloadModel) mustBe withIDResponseJson
    }
  }
}
