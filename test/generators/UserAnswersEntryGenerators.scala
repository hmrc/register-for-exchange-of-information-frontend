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

package generators

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryWhatIsTradingNameUserAnswersEntry: Arbitrary[(pages.WhatIsTradingNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsTradingNamePage.type]
        value <- arbitrary[models.WhatIsTradingName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessHaveDifferentNameUserAnswersEntry: Arbitrary[(pages.BusinessHaveDifferentNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessHaveDifferentNamePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessWithoutIDNameUserAnswersEntry: Arbitrary[(pages.BusinessWithoutIDNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessWithoutIDNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourPostcodeUserAnswersEntry: Arbitrary[(pages.WhatIsYourPostcodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourPostcodePage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNonUkNameUserAnswersEntry: Arbitrary[(pages.NonUkNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.NonUkNamePage.type]
        value <- arbitrary[models.NonUkName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouLiveInTheUKUserAnswersEntry: Arbitrary[(pages.DoYouLiveInTheUKPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.DoYouLiveInTheUKPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySoleDateOfBirthUserAnswersEntry: Arbitrary[(pages.SoleDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SoleDateOfBirthPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySoleNameUserAnswersEntry: Arbitrary[(pages.SoleNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SoleNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddressUKUserAnswersEntry: Arbitrary[(pages.AddressUKPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.AddressUKPage.type]
        value <- arbitrary[models.Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddressWithoutIdUserAnswersEntry: Arbitrary[(pages.AddressWithoutIdPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.AddressWithoutIdPage.type]
        value <- arbitrary[models.Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourDateOfBirthUserAnswersEntry: Arbitrary[(pages.WhatIsYourDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourNameUserAnswersEntry: Arbitrary[(pages.WhatIsYourNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourNamePage.type]
        value <- arbitrary[models.Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourNationalInsuranceNumberUserAnswersEntry: Arbitrary[(pages.WhatIsYourNationalInsuranceNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatIsYourNationalInsuranceNumberPage.type]
        value <- arbitrary[Nino].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsThisYourBusinessUserAnswersEntry: Arbitrary[(pages.IsThisYourBusinessPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IsThisYourBusinessPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessNameUserAnswersEntry: Arbitrary[(pages.BusinessNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUTRUserAnswersEntry: Arbitrary[(pages.UTRPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.UTRPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBussinessTypeUserAnswersEntry: Arbitrary[(pages.BusinessTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.BusinessTypePage.type]
        value <- arbitrary[models.BusinessType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveUniqueTaxPayerReferenceUserAnswersEntry: Arbitrary[(pages.DoYouHaveUniqueTaxPayerReferencePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.DoYouHaveUniqueTaxPayerReferencePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatAreYouRegisteringAsUserAnswersEntry: Arbitrary[(pages.WhatAreYouRegisteringAsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.WhatAreYouRegisteringAsPage.type]
        value <- arbitrary[models.WhatAreYouRegisteringAs].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveNINUserAnswersEntry: Arbitrary[(pages.DoYouHaveNINPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.DoYouHaveNINPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndConHavePhoneUserAnswersEntry: Arbitrary[(pages.SndConHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SndConHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactPhoneUserAnswersEntry: Arbitrary[(pages.SndContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SndContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactEmailUserAnswersEntry: Arbitrary[(pages.SndContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SndContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySndContactNameUserAnswersEntry: Arbitrary[(pages.SndContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SndContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactUserAnswersEntry: Arbitrary[(pages.SecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsContactTelephoneUserAnswersEntry: Arbitrary[(pages.IsContactTelephonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IsContactTelephonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactPhoneUserAnswersEntry: Arbitrary[(pages.ContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(pages.ContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactEmailUserAnswersEntry: Arbitrary[(pages.ContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

}
