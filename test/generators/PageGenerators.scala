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

trait PageGenerators {

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
