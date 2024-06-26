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

package models.subscription.response

import play.api.libs.json.{Reads, __}

case class SubscriptionIDResponse(subscriptionID: String)

object SubscriptionIDResponse {

  implicit val reads: Reads[SubscriptionIDResponse] = {
    import play.api.libs.functional.syntax._
    (__ \ "responseDetail" \ "subscriptionID").read[String] fmap SubscriptionIDResponse.apply
  }
}
