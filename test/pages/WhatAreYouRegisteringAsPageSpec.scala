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

import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.{Address, AddressLookup, Country, Name, NonUkName, UniqueTaxpayerReference, UserAnswers, WhatAreYouRegisteringAs}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class WhatAreYouRegisteringAsPageSpec extends PageBehaviours {

  "WhatAreYouRegisteringAsPage" - {

    beRetrievable[WhatAreYouRegisteringAs](WhatAreYouRegisteringAsPage)

    beSettable[WhatAreYouRegisteringAs](WhatAreYouRegisteringAsPage)

    beRemovable[WhatAreYouRegisteringAs](WhatAreYouRegisteringAsPage)
  }

  "cleanup" - {

    "must remove 'individual' journey when 'A business' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
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
            .set(NonUkNamePage, NonUkName("firstName", "lastName"))
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
            .set(IndividualAddressWithoutIdPage, Address("addressLine1", None, "addressLine2", None, None, Country("", "UK", "United Kingdom")))
            .success
            .value
            .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
            .success
            .value

          result.get(WhatIsYourNationalInsuranceNumberPage) must not be defined
          result.get(WhatIsYourNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
          result.get(NonUkNamePage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(WhatIsYourPostcodePage) must not be defined
          result.get(AddressLookupPage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(AddressUKPage) must not be defined
          result.get(IndividualAddressWithoutIdPage) must not be defined
      }
    }

    "must remove 'business' journey when 'an individual' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(UTRPage, UniqueTaxpayerReference("123"))
            .success
            .value
            .set(BusinessNamePage, "businessName")
            .success
            .value
            .set(SoleNamePage, Name("firstName", "lastName"))
            .success
            .value
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(BusinessWithoutIDNamePage, "businessName")
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, "tradingName")
            .success
            .value
            .set(BusinessAddressWithoutIdPage, Address("addressLine1", None, "addressLine2", None, None, Country("", "UK", "United Kingdom")))
            .success
            .value
            .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
            .success
            .value

          result.get(UTRPage) must not be defined
          result.get(BusinessNamePage) must not be defined
          result.get(SoleNamePage) must not be defined
          result.get(IsThisYourBusinessPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
          result.get(BusinessAddressWithoutIdPage) must not be defined
      }
    }
  }
}
