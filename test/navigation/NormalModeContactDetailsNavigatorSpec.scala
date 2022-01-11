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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models.BusinessType.{LimitedCompany, Sole}
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NormalModeContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from Contact Name page to Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactNamePage, NormalMode, MDR, answers)
              .mustBe(routes.ContactEmailController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Contact Email page to IsContactTelephone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactEmailPage, NormalMode, MDR, answers)
              .mustBe(routes.IsContactTelephoneController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from IsContactTelephone page to Contact Phone page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(IsContactTelephonePage, true)
                .success
                .value

            navigator
              .nextPage(IsContactTelephonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.ContactPhoneController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from IsContactTelephone page to Second Contact Phone page if NO is selected and they are a business" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                .success
                .value
                .set(BusinessTypePage, LimitedCompany)
                .success
                .value
                .set(IsContactTelephonePage, false)
                .success
                .value

            navigator
              .nextPage(IsContactTelephonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SecondContactController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from IsContactTelephone page to to the check your answers page if NO is selected and they are an individual" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
                .success
                .value
                .set(IsContactTelephonePage, false)
                .success
                .value

            navigator
              .nextPage(IsContactTelephonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from IsContactTelephone page to to the check your answers page if NO is selected and they are a sole proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                .success
                .value
                .set(BusinessTypePage, Sole)
                .success
                .value
                .set(IsContactTelephonePage, false)
                .success
                .value

            navigator
              .nextPage(IsContactTelephonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from telephone number page to second contact name page if they have a UTR and are not a sole proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                .success
                .value
                .set(BusinessTypePage, LimitedCompany)
                .success
                .value

            navigator
              .nextPage(ContactPhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SecondContactController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from telephone number page to the check your answers page if they have a UTR and they are a sole proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                .success
                .value
                .set(BusinessTypePage, Sole)
                .success
                .value

            navigator
              .nextPage(ContactPhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from telephone number page to second contact name page if they do not have a UTR and are registering as a business" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
                .success
                .value

            navigator
              .nextPage(ContactPhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SecondContactController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from telephone number page to the check you answers page if they do not have a UTR and are registering as an individual" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
                .success
                .value

            navigator
              .nextPage(ContactPhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Second Contact page to Second Contact Name page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SecondContactPage, true)
                .success
                .value

            navigator
              .nextPage(SecondContactPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SndContactNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Second Contact page to CheckYourAnswers page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SecondContactPage, false)
                .success
                .value

            navigator
              .nextPage(SecondContactPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Second Contact Name page to Second Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SndContactNamePage, NormalMode, MDR, answers)
              .mustBe(routes.SndContactEmailController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Second Contact Email page to Second Contact Have Phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SndContactEmailPage, NormalMode, MDR, answers)
              .mustBe(routes.SndConHavePhoneController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Second Contact Have Phone page to Second Contact Phone page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SndConHavePhonePage, true)
                .success
                .value

            navigator
              .nextPage(SndConHavePhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SndContactPhoneController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Second Contact Have Phone page to CheckYourAnswers page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SndConHavePhonePage, false)
                .success
                .value

            navigator
              .nextPage(SndConHavePhonePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }
    }
  }
}
