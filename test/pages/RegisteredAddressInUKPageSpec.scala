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

import models.ReporterType.Sole
import models.{Address, AddressLookup, Country, Name, NonUkName, UserAnswers}
import models.matching.{OrgRegistrationInfo, SafeId}
import models.register.response.details.AddressResponse
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class RegisteredAddressInUKPageSpec extends PageBehaviours {

  "RegisteredAddressInUKPage" - {

    beRetrievable[Boolean](RegisteredAddressInUKPage)

    beSettable[Boolean](RegisteredAddressInUKPage)

    beRemovable[Boolean](RegisteredAddressInUKPage)
  }

  "cleanup" - {

    "must remove individual and organistion without ID details when user selectsyes to registered address in UK" in {
      val address       = Address("line 1", None, "line 3", None, None, Country("state", "DE", "desc"))
      val addressLookup = AddressLookup(None, None, None, None, "town", None, "postcode")

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(DoYouHaveUniqueTaxPayerReferencePage, false)
            .success
            .value
            .set(DoYouHaveNINPage, true)
            .success
            .value
            .set(WhatIsYourNationalInsuranceNumberPage, Nino("AA000000A"))
            .success
            .value
            .set(WhatIsYourNamePage, Name("first", "last"))
            .success
            .value
            .set(WhatIsYourDateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DateOfBirthWithoutIdPage, LocalDate.now())
            .success
            .value
            .set(NonUkNamePage, NonUkName("first", "last"))
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(WhatIsYourPostcodePage, "postcode")
            .success
            .value
            .set(IndividualAddressWithoutIdPage, address)
            .success
            .value
            .set(SelectAddressPage, "address")
            .success
            .value
            .set(SelectedAddressLookupPage, addressLookup)
            .success
            .value
            .set(AddressLookupPage, Seq(addressLookup))
            .success
            .value
            .set(AddressUKPage, address)
            .success
            .value
            .set(BusinessWithoutIDNamePage, "Name")
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, "trading name")
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(SafeId("safeId"), "Organisation", AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(BusinessAddressWithoutIdPage, address)
            .success
            .value
            .set(RegisteredAddressInUKPage, true)
            .success
            .value

          result.get(DoYouHaveUniqueTaxPayerReferencePage) must not be defined
          result.get(DoYouHaveNINPage) must not be defined
          result.get(WhatIsYourNationalInsuranceNumberPage) must not be defined
          result.get(WhatIsYourNamePage) must not be defined
          result.get(WhatIsYourDateOfBirthPage) must not be defined
          result.get(DateOfBirthWithoutIdPage) must not be defined
          result.get(NonUkNamePage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(WhatIsYourPostcodePage) must not be defined
          result.get(IndividualAddressWithoutIdPage) must not be defined
          result.get(SelectAddressPage) must not be defined
          result.get(SelectedAddressLookupPage) must not be defined
          result.get(AddressLookupPage) must not be defined
          result.get(AddressUKPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(BusinessAddressWithoutIdPage) must not be defined
      }
    }
  }
}
