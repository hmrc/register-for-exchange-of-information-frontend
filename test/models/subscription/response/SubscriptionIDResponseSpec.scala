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

package models.subscription.response

import base.SpecBase
import play.api.libs.json.Json

class SubscriptionIDResponseSpec extends SpecBase {

  "SubscriptionIDResponseSpec" - {
    "must read CreateSubscriptionForMDRResponse" in {
      val json = Json.parse("""{"createSubscriptionForMDRResponse": {"responseDetail":{"subscriptionID": "id"}}}""".stripMargin)
      json.as[CreateSubscriptionForMDRResponse] mustBe CreateSubscriptionForMDRResponse(SubscriptionIDResponse("id"))
    }

    "must read DisplaySubscriptionForMDRResponse" in {
      val json = Json.parse("""{"displaySubscriptionForMDRResponse": {"responseDetail":{"subscriptionID": "id"}}}""".stripMargin)
      json.as[DisplaySubscriptionForMDRResponse] mustBe DisplaySubscriptionForMDRResponse(SubscriptionIDResponse("id"))
    }
  }
}
