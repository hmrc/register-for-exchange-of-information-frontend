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

package generators

import org.scalacheck.Arbitrary

trait PageGenerators {

  implicit lazy val arbitraryIndividualContactPhonePage: Arbitrary[pages.IndividualContactPhonePage.type] =
    Arbitrary(pages.IndividualContactPhonePage)

  implicit lazy val arbitraryIndividualHaveContactTelephonePage: Arbitrary[pages.IndividualHaveContactTelephonePage.type] =
    Arbitrary(pages.IndividualHaveContactTelephonePage)

  implicit lazy val arbitraryIndividualContactEmailPage: Arbitrary[pages.IndividualContactEmailPage.type] =
    Arbitrary(pages.IndividualContactEmailPage)

  implicit lazy val arbitraryWhatIsTradingNamePage: Arbitrary[pages.WhatIsTradingNamePage.type] =
    Arbitrary(pages.WhatIsTradingNamePage)

  implicit lazy val arbitraryBusinessHaveDifferentNamePage: Arbitrary[pages.BusinessHaveDifferentNamePage.type] =
    Arbitrary(pages.BusinessHaveDifferentNamePage)

  implicit lazy val arbitraryBusinessWithoutIDNamePage: Arbitrary[pages.BusinessWithoutIDNamePage.type] =
    Arbitrary(pages.BusinessWithoutIDNamePage)

  implicit lazy val arbitraryWhatIsYourPostcodePage: Arbitrary[pages.WhatIsYourPostcodePage.type] =
    Arbitrary(pages.WhatIsYourPostcodePage)

  implicit lazy val arbitraryNonUkNamePage: Arbitrary[pages.NonUkNamePage.type] =
    Arbitrary(pages.NonUkNamePage)

  implicit lazy val arbitraryDoYouLiveInTheUKPage: Arbitrary[pages.DoYouLiveInTheUKPage.type] =
    Arbitrary(pages.DoYouLiveInTheUKPage)

  implicit lazy val arbitrarySoleDateOfBirthPage: Arbitrary[pages.SoleDateOfBirthPage.type] =
    Arbitrary(pages.SoleDateOfBirthPage)

  implicit lazy val arbitrarySoleNamePage: Arbitrary[pages.SoleNamePage.type] =
    Arbitrary(pages.SoleNamePage)

  implicit lazy val arbitraryAddressUKPage: Arbitrary[pages.AddressUKPage.type] =
    Arbitrary(pages.AddressUKPage)

  implicit lazy val arbitraryAddressWithoutIdPage: Arbitrary[pages.AddressWithoutIdPage.type] =
    Arbitrary(pages.AddressWithoutIdPage)

  implicit lazy val arbitraryWhatIsYourDateOfBirthPage: Arbitrary[pages.WhatIsYourDateOfBirthPage.type] =
    Arbitrary(pages.WhatIsYourDateOfBirthPage)

  implicit lazy val arbitraryWhatIsYourNamePage: Arbitrary[pages.WhatIsYourNamePage.type] =
    Arbitrary(pages.WhatIsYourNamePage)

  implicit lazy val arbitraryWhatIsYourNationalInsuranceNumberPage: Arbitrary[pages.WhatIsYourNationalInsuranceNumberPage.type] =
    Arbitrary(pages.WhatIsYourNationalInsuranceNumberPage)

  implicit lazy val arbitraryIsThisYourBusinessPage: Arbitrary[pages.IsThisYourBusinessPage.type] =
    Arbitrary(pages.IsThisYourBusinessPage)

  implicit lazy val arbitraryBusinessNamePage: Arbitrary[pages.BusinessNamePage.type] =
    Arbitrary(pages.BusinessNamePage)

  implicit lazy val arbitraryUTRPage: Arbitrary[pages.UTRPage.type] =
    Arbitrary(pages.UTRPage)

  implicit lazy val arbitraryBussinessTypePage: Arbitrary[pages.BusinessTypePage.type] =
    Arbitrary(pages.BusinessTypePage)

  implicit lazy val arbitraryDoYouHaveUniqueTaxPayerReferencePage: Arbitrary[pages.DoYouHaveUniqueTaxPayerReferencePage.type] =
    Arbitrary(pages.DoYouHaveUniqueTaxPayerReferencePage)

  implicit lazy val arbitraryWhatAreYouRegisteringAsPage: Arbitrary[pages.WhatAreYouRegisteringAsPage.type] =
    Arbitrary(pages.WhatAreYouRegisteringAsPage)

  implicit lazy val arbitraryDoYouHaveNINPage: Arbitrary[pages.DoYouHaveNINPage.type] =
    Arbitrary(pages.DoYouHaveNINPage)

  implicit lazy val arbitrarySndConHavePhonePage: Arbitrary[pages.SndConHavePhonePage.type] =
    Arbitrary(pages.SndConHavePhonePage)

  implicit lazy val arbitrarySndContactPhonePage: Arbitrary[pages.SndContactPhonePage.type] =
    Arbitrary(pages.SndContactPhonePage)

  implicit lazy val arbitrarySndContactEmailPage: Arbitrary[pages.SndContactEmailPage.type] =
    Arbitrary(pages.SndContactEmailPage)

  implicit lazy val arbitrarySndContactNamePage: Arbitrary[pages.SndContactNamePage.type] =
    Arbitrary(pages.SndContactNamePage)

  implicit lazy val arbitrarySecondContactPage: Arbitrary[pages.SecondContactPage.type] =
    Arbitrary(pages.SecondContactPage)

  implicit lazy val arbitraryIsContactTelephonePage: Arbitrary[pages.IsContactTelephonePage.type] =
    Arbitrary(pages.IsContactTelephonePage)

  implicit lazy val arbitraryContactPhonePage: Arbitrary[pages.ContactPhonePage.type] =
    Arbitrary(pages.ContactPhonePage)

  implicit lazy val arbitraryContactNamePage: Arbitrary[pages.ContactNamePage.type] =
    Arbitrary(pages.ContactNamePage)

  implicit lazy val arbitraryContactEmailPage: Arbitrary[pages.ContactEmailPage.type] =
    Arbitrary(pages.ContactEmailPage)

}
