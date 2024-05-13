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

package models.subscription

import base.SpecBase
import helpers.RegisterHelper.registrationWithIDOrganisationResponse
import play.api.libs.json.{JsValue, Json}

class BusinessDetailsSpec extends SpecBase {

  val businessAddress: BusinessAddress =
    BusinessAddress("100 Parliament Street", None, None, Some("London"), "SW1A 2BQ", "GB")

  val businessDetails: BusinessDetails = BusinessDetails(
    OrgName,
    businessAddress
  )

  "BusinessDetails" - {

    "must serialise BusinessDetails" in {
      val json: JsValue =
        Json.parse(
          s"""
            |{"name":"$OrgName","address":{"addressLine1":"100 Parliament Street","addressLine4":"London","postCode":"SW1A 2BQ","countryCode":"GB"}}""".stripMargin
        )

      Json.toJson(businessDetails) mustBe json
    }

    "must de-serialise BusinessDetails" in {
      val json: JsValue =
        Json.parse(
          s"""
            |{"organisation": {"organisationName": "$OrgName"},"address":{"addressLine1":"100 Parliament Street","addressLine4":"London","postalCode":"SW1A 2BQ","countryCode":"GB"}}""".stripMargin
        )

      json.as[BusinessDetails] mustBe businessDetails
    }

    "fromRegistrationMatch must create a BusinessAddress from an AddressResponse" in {
      BusinessDetails.fromRegistrationMatch(registrationWithIDOrganisationResponse) mustBe Some(businessDetails)
    }
  }
}
