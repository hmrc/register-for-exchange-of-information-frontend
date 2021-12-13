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

import models.{Address, AddressLookup, Country, Name, NonUkName, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class DoYouHaveNINPageSpec extends PageBehaviours {

  "DoYouHaveNINPage" - {

    beRetrievable[Boolean](DoYouHaveNINPage)

    beSettable[Boolean](DoYouHaveNINPage)

    beRemovable[Boolean](DoYouHaveNINPage)
  }

  "cleanup" - {

    "must remove with NINO journey when 'No' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(WhatIsYourNationalInsuranceNumberPage, Nino("CS700100A"))
            .success
            .value
            .set(WhatIsYourNamePage, Name("firstName", "lastName"))
            .success
            .value
            .set(WhatIsYourDateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DoYouHaveNINPage, false, Some(true))
            .success
            .value

          result.get(WhatIsYourNationalInsuranceNumberPage) must not be defined
          result.get(WhatIsYourNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
      }
    }

    "must remove without NINO journey when 'Yes' is selected" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(NonUkNamePage, NonUkName("firstName", "lastName"))
            .success
            .value
            .set(WhatIsYourDateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(WhatIsYourPostcodePage, "NE11QZ")
            .success
            .value
            .set(AddressUKPage, Address("", None, "", None, None, Country("", "", "")))
            .success
            .value
            .set(AddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
            .success
            .value
            .set(SelectAddressPage, "SomeAddress")
            .success
            .value
            .set(SelectedAddressLookupPage, AddressLookup(None, None, None, None, "", None, ""))
            .success
            .value
            .set(DoYouHaveNINPage, true, Some(false))
            .success
            .value

          result.get(NonUkNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(WhatIsYourPostcodePage) must not be defined
          result.get(AddressUKPage) must not be defined
          result.get(AddressWithoutIdPage) must not be defined
          result.get(SelectAddressPage) must not be defined
          result.get(SelectedAddressLookupPage) must not be defined
      }
    }
  }
}
