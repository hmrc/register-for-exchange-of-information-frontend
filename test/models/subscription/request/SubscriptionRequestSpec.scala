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

package models.subscription.request

import base.SpecBase
import generators.Generators
import models.matching.MatchingInfo
import models.{NonUkName, UserAnswers}
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
      val subscriptionRequestCommon = SubscriptionRequestCommon("MDR", "2021-10-22T00:40:06Z", "d64d76beb68349d19931035ace5b4582", "MDTP", None)
      val requestDtls               = CreateRequestDetail("SAFE", "safeId", None, true, PrimaryContact(OrganisationDetails("Name Name"), "test@test.com", None, None), None)

      val expectedRequest = SubscriptionRequest(subscriptionRequestCommon, requestDtls)

      val userAnswers = UserAnswers("id")
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(NonUkNamePage, NonUkName("fred", "smith"))
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(MatchingInfoPage, MatchingInfo("safeId", None, None))
        .success
        .value

      val subscriptionRequest = SubscriptionRequest.convertTo(userAnswers).value
      subscriptionRequest.requestCommon.regime mustBe "MDR"
      subscriptionRequest.requestCommon.originatingSystem mustBe "MDTP"
      subscriptionRequest.requestDetail mustBe requestDtls
    }

    "must return None for missing 'UserAnswers'" in {
      val userAnswers = UserAnswers("id")
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(NonUkNamePage, NonUkName("fred", "smith"))
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value

      val subscriptionRequest = SubscriptionRequest.convertTo(userAnswers)
      subscriptionRequest mustBe None
    }
  }
}