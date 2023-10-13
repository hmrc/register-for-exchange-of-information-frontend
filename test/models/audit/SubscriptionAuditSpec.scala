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

package models.audit

import base.SpecBase
import generators.Generators
import models.IdentifierType.SAFE
import models.ReporterType.Individual
import models.subscription.request.{ContactInformation, CreateRequestDetail, IndividualDetails, OrganisationDetails}
import models.{Address, Country, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class SubscriptionAuditSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "SubscriptionAudit" - {
    "convert to SubscriptionAudit" in {

      val requestDtls = CreateRequestDetail(
        SAFE,
        "AB123456Z",
        Some("Tools for Traders Limited"),
        true,
        ContactInformation(IndividualDetails(name.firstName, None, name.lastName), TestEmail, Some(TestPhoneNumber), Some(TestMobilePhoneNumber)),
        Some(ContactInformation(OrganisationDetails("Tools for Traders"), "contact@toolsfortraders.com", Some(TestPhoneNumber), None))
      )
      val auditResponse = AuditResponse("Success", 200, Some("sub"), None)
      val address       = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(ReporterTypePage, Individual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, nonUkName)
        .success
        .value
        .set(IndividualContactEmailPage, TestEmail)
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value
        .set(IndividualAddressWithoutIdPage, address)
        .success
        .value
      val result = SubscriptionAudit.apply(userAnswers = userAnswers, requestDtls, auditResponse = auditResponse)

      result mustBe SubscriptionAudit(
        SAFEID = "AB123456Z",
        UTR = " ",
        NINO = " ",
        saUTR = " ",
        isBusiness = false,
        tradingName = Some("Tools for Traders Limited"),
        isGBUser = true,
        primaryContact =
          ContactInformation(IndividualDetails(name.firstName, None, name.lastName), TestEmail, Some(TestPhoneNumber), Some(TestMobilePhoneNumber)),
        secondaryContact = Some(ContactInformation(OrganisationDetails("Tools for Traders"), "contact@toolsfortraders.com", Some(TestPhoneNumber), None)),
        response = AuditResponse("Success", 200, Some("sub"), None)
      )

    }
  }
}
