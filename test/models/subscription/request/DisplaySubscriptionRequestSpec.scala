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

package models.subscription.request

import base.SpecBase
import play.api.libs.json.Json

class DisplaySubscriptionRequestSpec extends SpecBase {
  val params: Option[Seq[RequestParameter]]            = Some(Seq(RequestParameter("name", "value")))
  val requestDetail: RequestDetail                     = RequestDetail("id", "number")
  val requestCommon: SubscriptionRequestCommon         = SubscriptionRequestCommon("regime", "date", "ref", "MDTP", params, Some("uuid"))
  val readSubscriptionRequest: ReadSubscriptionRequest = ReadSubscriptionRequest(requestCommon, requestDetail)

  "DisplaySubscriptionRequest" - {
    "must write MDR request" in {
      val displaySubscriptionRequest: DisplaySubscriptionRequest = DisplaySubscriptionForMDRRequest(readSubscriptionRequest)
      val expectedJson = Json.parse(
        """
          |{
          |  "displaySubscriptionForMDRRequest": {
          |    "requestCommon": {
          |      "regime": "regime",
          |      "receiptDate": "date",
          |      "acknowledgementReference": "ref",
          |      "originatingSystem": "MDTP",
          |      "requestParameters": [
          |        {
          |          "paramName": "name",
          |          "paramValue": "value"
          |        }
          |      ],
          |      "conversationID": "uuid"
          |    },
          |    "requestDetail": {
          |      "IDType": "id",
          |      "IDNumber": "number"
          |    }
          |  }
          |}""".stripMargin
      )

      Json.toJson(displaySubscriptionRequest) mustBe expectedJson
    }

  }
}
