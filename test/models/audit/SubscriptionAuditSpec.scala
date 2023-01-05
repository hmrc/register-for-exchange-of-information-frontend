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
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.subscription.request.{ContactInformation, CreateRequestDetail, IndividualDetails, OrganisationDetails}
import models.{Address, Country, NonUkName, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class SubscriptionAuditSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "SubscriptionAudit" - {
    "convert to SubscriptionAudit" in {

      val requestDtls = CreateRequestDetail(
        "SAFE",
        "AB123456Z",
        Some("Tools for Traders Limited"),
        true,
        ContactInformation(IndividualDetails("John", None, "Smith"), "john@toolsfortraders.com", Some("0188899999"), Some("07321012345")),
        Some(ContactInformation(OrganisationDetails("Tools for Traders"), "contact@toolsfortraders.com", Some("0188899999"), None))
      )
      val auditResponse = AuditResponse("Success", 200, Some("sub"), None)
      val address       = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("a", "b"))
        .success
        .value
        .set(IndividualContactEmailPage, "test@gmail.com")
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
        primaryContact = ContactInformation(IndividualDetails("John", None, "Smith"), "john@toolsfortraders.com", Some("0188899999"), Some("07321012345")),
        secondaryContact = Some(ContactInformation(OrganisationDetails("Tools for Traders"), "contact@toolsfortraders.com", Some("0188899999"), None)),
        response = AuditResponse("Success", 200, Some("sub"), None)
      )

    }
  }
}
