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

import models.{MDR, Regime}
import play.api.libs.json._

sealed trait DisplaySubscriptionRequest

case class DisplaySubscriptionForMDRRequest(displaySubscriptionForMDRRequest: ReadSubscriptionRequest) extends DisplaySubscriptionRequest

object DisplaySubscriptionForMDRRequest {
  implicit lazy val writes: OWrites[DisplaySubscriptionForMDRRequest] = Json.writes[DisplaySubscriptionForMDRRequest]
}

case class DisplaySubscriptionForCBCRequest(displaySubscriptionForCBCRequest: ReadSubscriptionRequest) extends DisplaySubscriptionRequest

object DisplaySubscriptionForCBCRequest {
  implicit lazy val writes: OWrites[DisplaySubscriptionForCBCRequest] = Json.writes[DisplaySubscriptionForCBCRequest]
}

object DisplaySubscriptionRequest {

  implicit val writes: Writes[DisplaySubscriptionRequest] = Writes[DisplaySubscriptionRequest] {
    case i: DisplaySubscriptionForMDRRequest => Json.toJson(i)
    case o: DisplaySubscriptionForCBCRequest => Json.toJson(o)
  }

  def convertTo(regime: Regime, safeId: String): DisplaySubscriptionRequest =
    regime match {
      case MDR    => DisplaySubscriptionForMDRRequest(ReadSubscriptionRequest.createReadSubscriptionRequest(regime, safeId))
      case regime => throw new RuntimeException(s"Not supporting the regime: $regime ")
    }
}
