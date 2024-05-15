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
import models.{Address, Country, ReporterType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.Json

class CreateRequestDetailSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "CreateRequestDetail" - {
    "must serialise to json" in {
      val createRequestDetail = arbitrary[CreateRequestDetail].sample.value
      Json.toJson(createRequestDetail).as[CreateRequestDetail] mustBe createRequestDetail
    }

    "must de-serialise to json" in {

      val expectedRequestDtls = CreateRequestDetail(
        SAFE,
        "AB123456Z",
        Some("Tools for Traders Limited"),
        true,
        ContactInformation(
          IndividualDetails("John", None, "Smith"),
          "john@toolsfortraders.com",
          Some(TestPhoneNumber),
          Some(TestMobilePhoneNumber)
        ),
        Some(
          ContactInformation(
            OrganisationDetails("Tools for Traders"),
            "contact@toolsfortraders.com",
            Some(TestPhoneNumber),
            None
          )
        )
      )

      val json: String =
        s"""
          {
          |   "IDType": "$SAFE",
          |   "IDNumber": "AB123456Z",
          |   "tradingName": "Tools for Traders Limited",
          |   "isGBUser": true,
          |   "primaryContact": {
          |    "$IndividualKey": {
          |     "firstName": "John",
          |     "lastName": "Smith"
          |    },
          |    "email": "john@toolsfortraders.com",
          |    "phone": "$TestPhoneNumber",
          |    "mobile": "$TestMobilePhoneNumber"
          |   },
          |   "secondaryContact": {
          |    "organisation": {
          |     "organisationName": "Tools for Traders"
          |    },
          |    "email": "contact@toolsfortraders.com",
          |    "phone": "$TestPhoneNumber"
          |   }
          |}""".stripMargin
      Json.parse(json).as[CreateRequestDetail] mustBe expectedRequestDtls
    }

    "must return 'CreateRequestDetail' for the input userAnswers" in {
      val createRequestDetails = CreateRequestDetail(
        IDType = SAFE,
        IDNumber = safeId.value,
        tradingName = Some("traderName"),
        isGBUser = true,
        primaryContact = ContactInformation(OrganisationDetails(OrgName), TestEmail, None, None),
        secondaryContact = None
      )

      val updatedUserAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(BusinessHaveDifferentNamePage, true)
        .success
        .value
        .set(WhatIsTradingNamePage, "traderName")
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

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value
      request mustBe createRequestDetails
    }

    "must create a request with the isGBUser flag set to true by UTR" in {
      val userAnswers        = emptyUserAnswers
      val updatedUserAnswers = userAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
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

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to true by Individual and has a NINO" in {
      val userAnswers        = emptyUserAnswers
      val updatedUserAnswers = userAnswers
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(IndividualContactEmailPage, TestEmail)
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to false by business without UTR not based in the UK" in {
      val businessAddress    = Address("", None, "", None, None, Country("valid", "DE", "Germany"))
      val updatedUserAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.LimitedCompany)
        .success
        .value
        .set(RegisteredAddressInUKPage, false)
        .success
        .value
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
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
        .set(BusinessAddressWithoutIdPage, businessAddress)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe false
    }

    "must create a request with the isGBUser flag set to true by Individual without NINO if address is UK" in {
      val userAnswers        = emptyUserAnswers
      val address            = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val updatedUserAnswers = userAnswers
        .set(ReporterTypePage, ReporterType.Individual)
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
        .set(DoYouLiveInTheUKPage, true)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to false by Individual without NINO if address is non UK" in {
      val userAnswers        = emptyUserAnswers
      val address            = Address("", None, "", None, None, Country("valid", "FR", "France"))
      val updatedUserAnswers = userAnswers
        .set(ReporterTypePage, ReporterType.Individual)
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
        .set(SecondContactPage, false)
        .success
        .value
      val request            = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe false
    }
  }
}
