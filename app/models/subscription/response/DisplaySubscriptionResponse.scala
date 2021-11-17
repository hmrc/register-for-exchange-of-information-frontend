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

import play.api.libs.json.{Json, Reads}

sealed trait DisplaySubscriptionResponse

object DisplaySubscriptionResponse {

  implicit lazy val reads: Reads[DisplaySubscriptionResponse] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {
      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    DisplaySubscriptionForMDRResponse.reads or
      DisplaySubscriptionForCBCResponse.reads
  }
}

case class DisplaySubscriptionForMDRResponse(displaySubscriptionForMDRResponse: SubscriptionIDResponse) extends DisplaySubscriptionResponse

object DisplaySubscriptionForMDRResponse {
  implicit lazy val reads: Reads[DisplaySubscriptionForMDRResponse] = Json.reads[DisplaySubscriptionForMDRResponse]
}

case class DisplaySubscriptionForCBCResponse(displaySubscriptionForCBCResponse: SubscriptionIDResponse) extends DisplaySubscriptionResponse

object DisplaySubscriptionForCBCResponse {
  implicit lazy val reads: Reads[DisplaySubscriptionForCBCResponse] = Json.reads[DisplaySubscriptionForCBCResponse]
}
