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
import generators.Generators
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.{Address, Country, Name, NonUkName, UserAnswers}
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
        "SAFE",
        "AB123456Z",
        Some("Tools for Traders Limited"),
        true,
        PrimaryContact(IndividualDetails("John", None, "Smith"), "john@toolsfortraders.com", Some("0188899999"), Some("07321012345")),
        Some(SecondaryContact(OrganisationDetails("Tools for Traders"), "contact@toolsfortraders.com", Some("0188899999"), None))
      )

      val json: String =
        """
          {
          |   "IDType": "SAFE",
          |   "IDNumber": "AB123456Z",
          |   "tradingName": "Tools for Traders Limited",
          |   "isGBUser": true,
          |   "primaryContact": {
          |    "individual": {
          |     "firstName": "John",
          |     "lastName": "Smith"
          |    },
          |    "email": "john@toolsfortraders.com",
          |    "phone": "0188899999",
          |    "mobile": "07321012345"
          |   },
          |   "secondaryContact": {
          |    "organisation": {
          |     "organisationName": "Tools for Traders"
          |    },
          |    "email": "contact@toolsfortraders.com",
          |    "phone": "0188899999"
          |   }
          |}""".stripMargin
      Json.parse(json).as[CreateRequestDetail] mustBe expectedRequestDtls
    }

    "must return 'CreateRequestDetail' for the input userAnswers" in {
      val createRequestDetails = CreateRequestDetail(
        IDType = "SAFE",
        IDNumber = "SAFEID",
        tradingName = Some("traderName"),
        isGBUser = true,
        primaryContact = PrimaryContact(OrganisationDetails("Name Name"), "test@test.com", None, None),
        secondaryContact = None
      )

      val updatedUserAnswers = UserAnswers("id")
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(BusinessHaveDifferentNamePage, true)
        .success
        .value
        .set(WhatIsTradingNamePage, "traderName")
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value
      request mustBe createRequestDetails
    }

    "must create a request with the isGBUser flag set to true by UTR" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to true by Individual and has a NINO" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(WhatIsYourNamePage, Name("name", "last"))
        .success
        .value
        .set(ContactEmailPage, "hello")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to true by business without UTR and based in the UK" in {
      val businessAddress = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val updatedUserAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
        .success
        .value
        .set(ContactEmailPage, "hello")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(BusinessAddressWithoutIdPage, businessAddress)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe true
    }

    "must create a request with the isGBUser flag set to false by business without UTR not based in the UK" in {
      val businessAddress = Address("", None, "", None, None, Country("valid", "DE", "Germany"))
      val updatedUserAnswers = UserAnswers("")
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
        .success
        .value
        .set(ContactEmailPage, "hello")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
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
      val userAnswers = UserAnswers("")
      val address     = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val updatedUserAnswers = userAnswers
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
        .set(ContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(BusinessAddressWithoutIdPage, address)
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
      val userAnswers = UserAnswers("")
      val address     = Address("", None, "", None, None, Country("valid", "FR", "France"))
      val updatedUserAnswers = userAnswers
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
        .set(ContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(BusinessAddressWithoutIdPage, address)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe false
    }

    "must create a request with the isGBUser flag set to false when criteria is missing" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(ContactEmailPage, "hello")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, true)
        .success
        .value
        .set(ContactPhonePage, "1122334455")
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val request = CreateRequestDetail.convertTo(safeId, updatedUserAnswers).value

      request.isGBUser mustBe false
    }

  }

}
