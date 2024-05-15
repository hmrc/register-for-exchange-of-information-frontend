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

package models.subscription.request

import base.SpecBase
import generators.Generators
import models.IdentifierType.SAFE
import models.UserAnswers
import models.matching.IndRegistrationInfo
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.Json

class SubscriptionRequestSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "SubscriptionRequest" - {
    "must serialise and de-serialise" in {
      val subscriptionRequest = arbitrary[SubscriptionRequest].sample.value
      Json.toJson(subscriptionRequest).as[SubscriptionRequest] mustBe subscriptionRequest
    }

    "must return SubscriptionRequest for the input 'UserAnswers'" in {
      val requestDtls =
        CreateRequestDetail(
          SAFE,
          safeId.value,
          None,
          true,
          ContactInformation(OrganisationDetails(OrgName), TestEmail, None, None),
          None
        )

      val userAnswers = UserAnswers("id")
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(NonUkNamePage, nonUkName)
        .success
        .value
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(ContactNamePage, OrgName)
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value
        .set(RegistrationInfoPage, IndRegistrationInfo(safeId))
        .success
        .value

      val subscriptionRequest = SubscriptionRequest.convertTo(safeId, userAnswers).value
      subscriptionRequest.requestCommon.regime mustBe "MDR"
      subscriptionRequest.requestCommon.originatingSystem mustBe "MDTP"
      subscriptionRequest.requestDetail mustBe requestDtls
    }

    "must return None for missing 'UserAnswers'" in {
      val userAnswers = UserAnswers("id")

      val subscriptionRequest = SubscriptionRequest.convertTo(safeId, userAnswers)
      subscriptionRequest mustBe None
    }
  }
}
