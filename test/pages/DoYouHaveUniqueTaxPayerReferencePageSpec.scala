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

package pages

import models.matching.MatchingInfo
import models.{Address, AddressLookup, BusinessType, Country, Name, NonUkName, UserAnswers, WhatAreYouRegisteringAs}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class DoYouHaveUniqueTaxPayerReferencePageSpec extends PageBehaviours {

  "DoYouHaveUniqueTaxPayerReferencePage" - {

    beRetrievable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beSettable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beRemovable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)
  }

  "cleanup" - {

    "must remove withID journey when 'No' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(BusinessTypePage, BusinessType.Sole)
            .success
            .value
            .set(UTRPage, "1234567890")
            .success
            .value
            .set(SoleNamePage, Name("firstName", "secondName"))
            .success
            .value
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(MatchingInfoPage, MatchingInfo("safeId", Some("businessName"), None))
            .success
            .value
            .set(BusinessNamePage, "businessName")
            .success
            .value
            .set(ContactNamePage, "contactName")
            .success
            .value
            .set(ContactEmailPage, "email@email.com")
            .success
            .value
            .set(IsContactTelephonePage, true)
            .success
            .value
            .set(ContactPhonePage, "07540000000")
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, "secondContactName")
            .success
            .value
            .set(SndContactEmailPage, "email2@email.com")
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, "07540000000")
            .success
            .value
            .set(DoYouHaveUniqueTaxPayerReferencePage, false)
            .success
            .value

          result.get(BusinessTypePage) must not be defined
          result.get(UTRPage) must not be defined
          result.get(SoleNamePage) must not be defined
          result.get(IsThisYourBusinessPage) must not be defined
          result.get(MatchingInfoPage) must not be defined
          result.get(BusinessNamePage) must not be defined
          result.get(ContactNamePage) must not be defined
          result.get(ContactEmailPage) must not be defined
          result.get(IsContactTelephonePage) must not be defined
          result.get(ContactPhonePage) must not be defined
          result.get(SecondContactPage) must not be defined
          result.get(SndContactNamePage) must not be defined
          result.get(SndContactEmailPage) must not be defined
          result.get(SndConHavePhonePage) must not be defined
          result.get(SndContactPhonePage) must not be defined
      }
    }

    "must remove withOutID journey when 'Yes' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
            .success
            .value
            .set(BusinessWithoutIDNamePage, "businessWithOutIDName")
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, "tradeName")
            .success
            .value
            .set(AddressWithoutIdPage, Address("addressLine1", None, "addressLine3", None, None, Country("", "UK", "United Kingdom")))
            .success
            .value
            .set(DoYouHaveNINPage, true)
            .success
            .value
            .set(WhatIsYourNationalInsuranceNumberPage, Nino("CS700100A"))
            .success
            .value
            .set(WhatIsYourNamePage, Name("firstName", "lastName"))
            .success
            .value
            .set(WhatIsYourDateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(NonUkNamePage, NonUkName("name", ""))
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(WhatIsYourPostcodePage, "NE324AS")
            .success
            .value
            .set(AddressLookupPage, Seq(AddressLookup(None, None, None, None, "Jarrow", None, "NE324AS")))
            .success
            .value
            .set(AddressUKPage, Address("addressLine1", None, "addressLine2", None, None, Country("", "UK", "United Kingdom")))
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(ContactNamePage, "contactName")
            .success
            .value
            .set(ContactEmailPage, "email@email.com")
            .success
            .value
            .set(IsContactTelephonePage, true)
            .success
            .value
            .set(ContactPhonePage, "07540000000")
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, "secondContactName")
            .success
            .value
            .set(SndContactEmailPage, "email2@email.com")
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, "07540000000")
            .success
            .value
            .set(DoYouHaveUniqueTaxPayerReferencePage, true)
            .success
            .value

          result.get(WhatAreYouRegisteringAsPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
          result.get(AddressWithoutIdPage) must not be defined
          result.get(DoYouHaveNINPage) must not be defined
          result.get(WhatIsYourNationalInsuranceNumberPage) must not be defined
          result.get(WhatIsYourNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
          result.get(NonUkNamePage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(WhatIsYourPostcodePage) must not be defined
          result.get(AddressLookupPage) must not be defined
          result.get(AddressUKPage) must not be defined
          result.get(ContactNamePage) must not be defined
          result.get(ContactEmailPage) must not be defined
          result.get(IsContactTelephonePage) must not be defined
          result.get(ContactPhonePage) must not be defined
          result.get(SecondContactPage) must not be defined
          result.get(SndContactNamePage) must not be defined
          result.get(SndContactEmailPage) must not be defined
          result.get(SndConHavePhonePage) must not be defined
          result.get(SndContactPhonePage) must not be defined
      }
    }

    "must retain withID journey when there is a change of the answer to 'Yes'" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(BusinessTypePage, BusinessType.Sole)
            .success
            .value
            .set(UTRPage, "1234567890")
            .success
            .value
            .set(SoleNamePage, Name("firstName", "secondName"))
            .success
            .value
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(MatchingInfoPage, MatchingInfo("safeId", Some("businessName"), None))
            .success
            .value
            .set(BusinessNamePage, "businessName")
            .success
            .value
            .set(ContactNamePage, "contactName")
            .success
            .value
            .set(ContactEmailPage, "email@email.com")
            .success
            .value
            .set(IsContactTelephonePage, true)
            .success
            .value
            .set(ContactPhonePage, "07540000000")
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, "secondContactName")
            .success
            .value
            .set(SndContactEmailPage, "email2@email.com")
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, "07540000000")
            .success
            .value
            .set(DoYouHaveUniqueTaxPayerReferencePage, true)
            .success
            .value

          result.get(BusinessTypePage) mustBe defined
          result.get(UTRPage) mustBe defined
          result.get(SoleNamePage) mustBe defined
          result.get(IsThisYourBusinessPage) mustBe defined
          result.get(MatchingInfoPage) mustBe defined
          result.get(BusinessNamePage) mustBe defined
          result.get(ContactNamePage) mustBe defined
          result.get(ContactEmailPage) mustBe defined
          result.get(IsContactTelephonePage) mustBe defined
          result.get(ContactPhonePage) mustBe defined
          result.get(SecondContactPage) mustBe defined
          result.get(SndContactNamePage) mustBe defined
          result.get(SndContactEmailPage) mustBe defined
          result.get(SndConHavePhonePage) mustBe defined
          result.get(SndContactPhonePage) mustBe defined
      }
    }
  }

}
