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

package models.matching

import base.SpecBase
import generators.Generators
import helpers.RegisterHelper
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class RegistrationInfoSpec extends SpecBase with Generators with ScalaCheckPropertyChecks with EitherValues {

  "RegistrationInfo" - {
    "must serialise and de-serialise OrgRegistrationInfo" in {
      val json: JsValue =
        Json.parse(
          s"""
            |{"safeId":"${safeId.value}","name":"$OrgName","address":{"addressLine1":"100 Parliament Street","addressLine4":"London","postalCode":"SW1A 2BQ","countryCode":"GB"},"_type":"models.matching.OrgRegistrationInfo"}""".stripMargin
        )

      val registrationInfo: RegistrationInfo = OrgRegistrationInfo(safeId, OrgName, RegisterHelper.addressResponse)
      Json.toJson(registrationInfo) mustBe json
      json.as[OrgRegistrationInfo] mustBe registrationInfo
    }

    "must serialise and de-serialise IndRegistrationInfo" in {
      val json: JsValue = Json.parse(s"""{"safeId":"${safeId.value}","_type":"models.matching.IndRegistrationInfo"}""".stripMargin)

      val registrationInfo: RegistrationInfo = IndRegistrationInfo(safeId)
      Json.toJson(registrationInfo) mustBe json
      json.as[IndRegistrationInfo] mustBe registrationInfo
    }
  }
}
