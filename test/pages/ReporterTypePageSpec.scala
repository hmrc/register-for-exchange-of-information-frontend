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

package pages

import models.ReporterType.{Individual, LimitedCompany, Sole}
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.{AddressLookup, ReporterType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class ReporterTypePageSpec extends PageBehaviours {
  implicit val reporterTypeArbitrary: Arbitrary[ReporterType] = Arbitrary(Gen.oneOf(ReporterType.values))

  "ReporterTypePage" - {

    beRetrievable[ReporterType](ReporterTypePage)

    beSettable[ReporterType](ReporterTypePage)

    beRemovable[ReporterType](ReporterTypePage)
  }

  "cleanup" - {

    "must remove organisation contact details when user selects a sole trader as reporter type" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(ContactNamePage, name.fullName)
            .success
            .value
            .set(ContactEmailPage, TestEmail)
            .success
            .value
            .set(ContactHavePhonePage, true)
            .success
            .value
            .set(ContactPhonePage, TestPhoneNumber)
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, name.fullName)
            .success
            .value
            .set(SndContactEmailPage, TestEmail)
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, TestMobilePhoneNumber)
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(BusinessWithoutIDNamePage, OrgName)
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, OrgName)
            .success
            .value
            .set(ReporterTypePage, Sole)
            .success
            .value

          result.get(IsThisYourBusinessPage) must not be defined
          result.get(ContactNamePage) must not be defined
          result.get(ContactEmailPage) must not be defined
          result.get(ContactHavePhonePage) must not be defined
          result.get(ContactPhonePage) must not be defined
          result.get(SecondContactPage) must not be defined
          result.get(SndContactNamePage) must not be defined
          result.get(SndContactEmailPage) must not be defined
          result.get(SndConHavePhonePage) must not be defined
          result.get(SndContactPhonePage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
      }
    }

    "must remove organisation contact details when user selects a Individual as reporter type" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(ContactNamePage, name.fullName)
            .success
            .value
            .set(ContactEmailPage, TestEmail)
            .success
            .value
            .set(ContactHavePhonePage, true)
            .success
            .value
            .set(ContactPhonePage, TestPhoneNumber)
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, name.fullName)
            .success
            .value
            .set(SndContactEmailPage, TestEmail)
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, TestMobilePhoneNumber)
            .success
            .value
            .set(BusinessWithoutIDNamePage, OrgName)
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, OrgName)
            .success
            .value
            .set(ReporterTypePage, Individual)
            .success
            .value

          result.get(IsThisYourBusinessPage) must not be defined
          result.get(ContactNamePage) must not be defined
          result.get(ContactEmailPage) must not be defined
          result.get(ContactHavePhonePage) must not be defined
          result.get(ContactPhonePage) must not be defined
          result.get(SecondContactPage) must not be defined
          result.get(SndContactNamePage) must not be defined
          result.get(SndContactEmailPage) must not be defined
          result.get(SndConHavePhonePage) must not be defined
          result.get(SndContactPhonePage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
      }
    }

    "must remove individual contact details when user selects any other organisation reporter type" in {

      val address = AddressLookup(None, None, None, None, "town", None, TestPostCode)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(DoYouHaveNINPage, false)
            .success
            .value
            .set(NonUkNamePage, nonUkName)
            .success
            .value
            .set(SoleNamePage, name)
            .success
            .value
            .set(WhatIsYourDateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(WhatIsYourPostcodePage, TestPostCode)
            .success
            .value
            .set(SelectAddressPage, "address")
            .success
            .value
            .set(SelectedAddressLookupPage, address)
            .success
            .value
            .set(AddressLookupPage, Seq(address))
            .success
            .value
            .set(IndividualContactEmailPage, TestEmail)
            .success
            .value
            .set(IndividualHaveContactTelephonePage, true)
            .success
            .value
            .set(IndividualContactPhonePage, TestPhoneNumber)
            .success
            .value
            .set(ReporterTypePage, LimitedCompany)
            .success
            .value

          result.get(IsThisYourBusinessPage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(DoYouHaveNINPage) must not be defined
          result.get(NonUkNamePage) must not be defined
          result.get(SoleNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(WhatIsYourPostcodePage) must not be defined
          result.get(SelectAddressPage) must not be defined
          result.get(SelectedAddressLookupPage) must not be defined
          result.get(AddressLookupPage) must not be defined
          result.get(IndividualContactEmailPage) must not be defined
          result.get(IndividualHaveContactTelephonePage) must not be defined
          result.get(IndividualContactPhonePage) must not be defined
      }
    }
  }
}
