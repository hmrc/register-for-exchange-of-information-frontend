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
import models.MDR
import org.scalatest.matchers.must.Matchers

class SubscriptionRequestCommonSpec extends SpecBase with Matchers {

  "SubscriptionRequestCommon" - {
    "must return create SubscriptionRequestCommon" in {
      val requestCommon = SubscriptionRequestCommon.createSubscriptionRequestCommon(MDR)

      requestCommon.regime mustBe "MDR"
      requestCommon.originatingSystem mustBe "MDTP"
      requestCommon.requestParameters mustBe None
      requestCommon.acknowledgementReference.nonEmpty mustBe true
      requestCommon.receiptDate.nonEmpty mustBe true
    }
  }
}
