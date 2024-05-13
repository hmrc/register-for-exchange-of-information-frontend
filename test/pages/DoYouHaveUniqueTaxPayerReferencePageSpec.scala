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

import models.ReporterType.LimitedCompany
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.{Address, AddressLookup, Country, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class DoYouHaveUniqueTaxPayerReferencePageSpec extends PageBehaviours {

  private val address =
    Address(
      "He lives in a house",
      Some("a very big house"),
      "In the country",
      Some("blur 1995"),
      Some("BritPop"),
      Country("", "GB", "Great Britain")
    )

  private val addressLookup = AddressLookup(
    Some("Your house was very small"),
    Some("with woodchip on the wall"),
    Some("and when I came round to call"),
    Some("you didn't notice me at all"),
    "Pulp 1995",
    Some("BritPop"),
    "D3B0R4H"
  )

  "DoYouHaveUniqueTaxPayerReferencePage" - {

    beRetrievable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beSettable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beRemovable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)
  }

  "cleanup" - {

    "must remove business with ID pages when user selects no to do you have a utr?" in {
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        val result = userAnswers
          .set(ReporterTypePage, LimitedCompany)
          .success
          .value
          .set(UTRPage, utr)
          .success
          .value
          .set(BusinessNamePage, OrgName)
          .success
          .value
          .set(IsThisYourBusinessPage, true)
          .success
          .value
          .set(
            RegistrationInfoPage,
            OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB"))
          )
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value

        result.get(UTRPage)                must not be defined
        result.get(BusinessNamePage)       must not be defined
        result.get(IsThisYourBusinessPage) must not be defined
        result.get(RegistrationInfoPage)   must not be defined
      }
    }

    "must remove individual pages when user selects YES to do you have a utr?" in {
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        val result = userAnswers
          .set(WhatIsYourNationalInsuranceNumberPage, Nino(TestNiNumber))
          .success
          .value
          .set(WhatIsYourNamePage, name)
          .success
          .value
          .set(WhatIsYourDateOfBirthPage, LocalDate.now())
          .success
          .value
          .set(DateOfBirthWithoutIdPage, LocalDate.now())
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value
          .set(NonUkNamePage, nonUkName)
          .success
          .value
          .set(DoYouLiveInTheUKPage, true)
          .success
          .value
          .set(WhatIsYourPostcodePage, TestPostCode)
          .success
          .value
          .set(IndividualAddressWithoutIdPage, address)
          .success
          .value
          .set(AddressLookupPage, Seq(addressLookup))
          .success
          .value
          .set(AddressUKPage, address)
          .success
          .value
          .set(SelectAddressPage, "true")
          .success
          .value
          .set(SelectedAddressLookupPage, addressLookup)
          .success
          .value
          .set(
            RegistrationInfoPage,
            OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB"))
          )
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        result.get(WhatIsYourNationalInsuranceNumberPage) must not be defined
        result.get(WhatIsYourNamePage)                    must not be defined
        result.get(WhatIsYourDateOfBirthPage)             must not be defined
        result.get(DateOfBirthWithoutIdPage)              must not be defined
        result.get(DoYouHaveNINPage)                      must not be defined
        result.get(NonUkNamePage)                         must not be defined
        result.get(DoYouLiveInTheUKPage)                  must not be defined
        result.get(WhatIsYourPostcodePage)                must not be defined
        result.get(IndividualAddressWithoutIdPage)        must not be defined
        result.get(AddressLookupPage)                     must not be defined
        result.get(AddressUKPage)                         must not be defined
        result.get(SelectAddressPage)                     must not be defined
        result.get(SelectedAddressLookupPage)             must not be defined
        result.get(RegistrationInfoPage)                  must not be defined
      }
    }

    "must remove business without ID pages when user selects YES to do you have a utr?" in {
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        val result = userAnswers
          .set(BusinessWithoutIDNamePage, OrgName)
          .success
          .value
          .set(BusinessHaveDifferentNamePage, true)
          .success
          .value
          .set(WhatIsTradingNamePage, OrgName)
          .success
          .value
          .set(DoYouLiveInTheUKPage, true)
          .success
          .value
          .set(BusinessAddressWithoutIdPage, address)
          .success
          .value
          .set(
            RegistrationInfoPage,
            OrgRegistrationInfo(safeId, OrgName, AddressResponse("Address", None, None, None, None, "GB"))
          )
          .success
          .value
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        result.get(BusinessWithoutIDNamePage)     must not be defined
        result.get(BusinessHaveDifferentNamePage) must not be defined
        result.get(WhatIsTradingNamePage)         must not be defined
        result.get(DoYouLiveInTheUKPage)          must not be defined
        result.get(BusinessAddressWithoutIdPage)  must not be defined
        result.get(RegistrationInfoPage)          must not be defined
      }
    }
  }
}
