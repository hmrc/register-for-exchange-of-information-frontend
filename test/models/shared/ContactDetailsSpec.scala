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

package models.shared

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class ContactDetailsSpec extends SpecBase {

  "ContactDetails" - {
    "must serialise and de-serialise ContactDetails" in {
      val json: JsValue =
        Json.parse(
          """
            |{"phoneNumber":"0987654321","mobileNumber":"0987654322","faxNumber": "0987654323","emailAddress":"test@test.com"}""".stripMargin
        )

      val contactDetails: ContactDetails = ContactDetails(Some("0987654321"), Some("0987654322"), Some("0987654323"), Some("test@test.com"))
      Json.toJson(contactDetails) mustBe json
      json.as[ContactDetails] mustBe contactDetails
    }

    "apply method must create a ContactDetails from a phoneNumber and emailAddress" in {
      ContactDetails(Some("0987654321"), Some("test@test.com")) mustBe ContactDetails(Some("0987654321"), None, None, Some("test@test.com"))
    }
  }
}
