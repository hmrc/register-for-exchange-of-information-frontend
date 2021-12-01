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

package utils

import base.{ControllerMockFixtures, SpecBase}
import config.FrontendAppConfig
import models.matching.{MatchingType, RegistrationInfo}
import models.register.response.details.AddressResponse
import models.{Address, Country, MDR, Name, NonUkName, UserAnswers, WhatAreYouRegisteringAs}
import org.mockito.ArgumentMatchers.any
import pages.{RegistrationInfoPage, _}
import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

import java.io.ByteArrayInputStream
import java.time.LocalDate

class CheckYourAnswersHelperSpec extends SpecBase with ControllerMockFixtures {
  // todo to sie kladzie
  val conf: FrontendAppConfig = mock[FrontendAppConfig]
  val env                     = mock[Environment]
  val countryListFactory      = new CountryListFactory(env, conf)

  // first contact
  val firstContactName  = "first-contact-name"
  val firstContactEmail = "first-contact-email"
  val firstContactPhone = "+44 0808 157 0192"

  // second contact
  val isSecondContact    = true
  val secondContactName  = "second-contact-name"
  val secondContactEmail = "second-contact-email"
  val secondContactPhone = "+44 0808 157 0193"

  val address          = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), Country("valid", "GB", "United Kingdom"))
  val dob              = LocalDate.now()
  val yes              = "yes"
  val no               = "no"
  val nonUkName        = NonUkName("givenName", "familyName")
  val nino             = Nino("CC123456C")
  val name             = Name("firstName", "lastName")
  val addressResponse  = AddressResponse("adr", None, None, None, None, "XX")
  val registrationInfo = RegistrationInfo("safeId", Some("name"), Some(addressResponse), MatchingType.AsIndividual)

  "CheckYourAnswersHelper  must " - {
    "build IndividualWithID" in {
      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(WhatIsYourNationalInsuranceNumberPage, nino)
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(WhatIsYourDateOfBirthPage, dob)
        .success
        .value

      val helper           = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val individualWithID = helper.buildIndividualWithID
      individualWithID.doYouHaveUniqueTaxPayerReference.get mustEqual false
      individualWithID.whatAreYouRegisteringAs.get mustEqual WhatAreYouRegisteringAs.RegistrationTypeIndividual
      individualWithID.doYouHaveNIN.get mustEqual false
      individualWithID.nino.get mustEqual nino
      individualWithID.whatIsYourName.get mustEqual name
      individualWithID.whatIsYourDateOfBirth.get mustEqual dob

      val asRow = individualWithID.asRowSeq
      asRow.length mustEqual 6
      asRow(0).value.toString.contains(no) mustBe true
      asRow(1).value.toString.contains(WhatAreYouRegisteringAs.RegistrationTypeIndividual.toString) mustBe true
      asRow(2).value.toString.contains(no) mustBe true
      asRow(3).value.toString.contains(nino.nino) mustBe true
      asRow(4).value.toString.contains(name.firstName) mustBe true
      asRow(5).value.toString.contains(dob.getYear.toString) mustBe true

    }

    "build IndividualWithoutID" in {
      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, nonUkName)
        .success
        .value
        .set(WhatIsYourDateOfBirthPage, dob)
        .success
        .value
        .set(AddressWithoutIdPage, address)
        .success
        .value
        .set(SelectAddressPage, "selectAddress")
        .success
        .value
        .set(AddressUKPage, address)
        .success
        .value

      val helper              = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val individualWithoutId = helper.buildIndividualWithoutID
      individualWithoutId.doYouHaveUniqueTaxPayerReference.get mustEqual false
      individualWithoutId.whatAreYouRegisteringAs.get mustEqual WhatAreYouRegisteringAs.RegistrationTypeIndividual
      individualWithoutId.doYouHaveNIN.get mustEqual false
      individualWithoutId.nonUkName.get mustEqual nonUkName
      individualWithoutId.whatIsYourDateOfBirth.get mustEqual dob
      individualWithoutId.addressWithoutIdIndividual.get mustEqual address
      individualWithoutId.addressUK.get mustEqual address
      individualWithoutId.selectAddress.get mustEqual "selectAddress"

      val asRow = individualWithoutId.asRowSeq
      asRow.length mustEqual 8
      asRow(0).value.toString.contains(no) mustBe true
      asRow(1).value.toString.contains(WhatAreYouRegisteringAs.RegistrationTypeIndividual.toString) mustBe true
      asRow(2).value.toString.contains(no) mustBe true
      asRow(3).value.toString.contains("givenName") mustBe true
      asRow(4).value.toString.contains(dob.getYear.toString) mustBe true
      asRow(5).value.toString.contains(address.addressLine1) mustBe true
      asRow(6).value.toString.contains(address.addressLine1) mustBe true
      asRow(7).value.toString.contains("selectAddress") mustBe true
    }

    "build BusinessWithID" in {
      val countries = Json.arr(Json.obj("state" -> "valid", "code" -> "XX", "description" -> "Somewhere"))
      when(conf.countryCodeJson).thenReturn("countries.json")
      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, registrationInfo)
        .success
        .value

      val helper         = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val businessWithID = helper.buildBusinessWithID
      businessWithID.confirmBusiness.get mustEqual true
      businessWithID.registrationInfo.get mustEqual registrationInfo

      val asRow = businessWithID.asRowSeq
      asRow.length mustEqual 1
      asRow(0).value.toString.contains("Somewhere") mustBe true
    }

    "build BusinessWithoutID" in {
      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
        .success
        .value
        .set(BusinessWithoutIDNamePage, "businessWithoutIDNamePage")
        .success
        .value
        .set(WhatIsTradingNamePage, "whatIsTradingName")
        .success
        .value
        .set(AddressWithoutIdPage, address)
        .success
        .value

      val helper            = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val businessWithoutID = helper.buildBusinessWithoutID
      businessWithoutID.doYouHaveUniqueTaxPayerReference.get mustEqual false
      businessWithoutID.whatAreYouRegisteringAs.get mustEqual WhatAreYouRegisteringAs.RegistrationTypeBusiness
      businessWithoutID.businessWithoutIDName.get mustEqual "businessWithoutIDNamePage"
      businessWithoutID.whatIsTradingName.get mustEqual "whatIsTradingName"
      businessWithoutID.addressWithoutIdBusiness.get mustEqual address

      val asRow = businessWithoutID.asRowSeq
      asRow.length mustEqual 5
      asRow(0).value.toString.contains(no) mustBe true
      asRow(1).value.toString.contains(WhatAreYouRegisteringAs.RegistrationTypeBusiness.toString) mustBe true
      asRow(2).value.toString.contains("businessWithoutIDNamePage") mustBe true
      asRow(3).value.toString.contains("whatIsTradingName") mustBe true
      asRow(4).value.toString.contains(address.addressLine1) mustBe true
    }

    "build AllPages" in {
      val countries = Json.arr(Json.obj("state" -> "valid", "code" -> "XX", "description" -> "Somewhere"))
      when(conf.countryCodeJson).thenReturn("countries.json")
      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, registrationInfo)
        .success
        .value
        .set(WhatIsYourNationalInsuranceNumberPage, nino)
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(WhatIsYourDateOfBirthPage, dob)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
        .success
        .value
        .set(BusinessWithoutIDNamePage, "businessWithoutIDNamePage")
        .success
        .value
        .set(AddressUKPage, address)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, nonUkName)
        .success
        .value
        .set(DoYouLiveInTheUKPage, true)
        .success
        .value

      val helper   = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val allPages = helper.buildAllPages
      allPages.confirmBusiness.get mustEqual true
      allPages.registrationInfo.get mustEqual registrationInfo
      allPages.nino.get mustEqual nino
      allPages.whatIsYourName.get mustEqual name
      // todo here rest of above

      val asRow = allPages.asRowSeq
      asRow.length mustEqual 11
      asRow(0).value.toString.contains(no) mustBe true
      asRow(1).value.toString.contains("Somewhere") mustBe true
      asRow(2).value.toString.contains(nino.nino) mustBe true
      asRow(3).value.toString.contains(name.firstName) mustBe true
      asRow(4).value.toString.contains(dob.getYear.toString) mustBe true
      asRow(5).value.toString.contains(WhatAreYouRegisteringAs.RegistrationTypeBusiness.toString) mustBe true
      asRow(6).value.toString.contains("businessWithoutIDNamePage") mustBe true
      asRow(7).value.toString.contains(address.addressLine1) mustBe true
      asRow(8).value.toString.contains(no) mustBe true
      asRow(9).value.toString.contains("givenName") mustBe true
      asRow(10).value.toString.contains(yes) mustBe true
    }

    "build FirstContact" in {
      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(ContactNamePage, firstContactName)
        .success
        .value
        .set(ContactEmailPage, firstContactEmail)
        .success
        .value
        .set(ContactPhonePage, firstContactPhone)
        .success
        .value

      val helper = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val fstCnt = helper.buildFirstContact
      fstCnt.contactName.get mustEqual firstContactName
      fstCnt.contactEmail.get mustEqual firstContactEmail
      fstCnt.contactPhone.get mustEqual firstContactPhone

      val asRow = fstCnt.asRowSeq
      asRow.length mustEqual 3
      asRow(0).value.toString.contains(firstContactName) mustBe true
      asRow(1).value.toString.contains(firstContactEmail) mustBe true
      asRow(2).value.toString.contains(firstContactPhone) mustBe true
    }

    "build SecondContact" in {
      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(SecondContactPage, isSecondContact)
        .success
        .value
        .set(SndContactNamePage, secondContactName)
        .success
        .value
        .set(SndContactEmailPage, secondContactEmail)
        .success
        .value
        .set(SndContactPhonePage, secondContactPhone)
        .success
        .value

      val helper = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
      val sndCnt = helper.buildSecondContact
      sndCnt.secondContact.get mustEqual isSecondContact
      sndCnt.sndContactName.get mustEqual secondContactName
      sndCnt.sndContactEmail.get mustEqual secondContactEmail
      sndCnt.sndContactPhone.get mustEqual secondContactPhone

      val asRow = sndCnt.asRowSeq
      asRow.length mustEqual 4
      asRow(0).value.toString.contains(yes) mustBe true
      asRow(1).value.toString.contains(secondContactName) mustBe true
      asRow(2).value.toString.contains(secondContactEmail) mustBe true
      asRow(3).value.toString.contains(secondContactPhone) mustBe true
    }
  }
}
