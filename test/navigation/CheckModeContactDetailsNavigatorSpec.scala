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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models.{BusinessType, CheckMode, MDR, NormalMode, UserAnswers, WhatAreYouRegisteringAs}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckModeContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, MDR, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad(MDR)
      }

      "must go from Contact Name page to CheckYourAnswers page " +
        "if user has NOT changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactNamePage, "someName")
                .success
                .value

              navigator
                .nextPageWithValueCheck(
                  ContactNamePage,
                  CheckMode,
                  MDR,
                  updatedAnswers,
                  originalValue = Some("someName")
                )
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from Contact Name page to 'What is the email address for?' page " +
        "if user has changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactNamePage, "someName")
                .success
                .value

              navigator
                .nextPageWithValueCheck(
                  ContactNamePage,
                  CheckMode,
                  MDR,
                  updatedAnswers,
                  originalValue = Some("originalName")
                )
                .mustBe(routes.ContactEmailController.onPageLoad(CheckMode, MDR))
          }
        }

      "must go from Contact Email page to CheckYourAnswers page " +
        "if user has NOT changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactEmailPage, "email@email.com")
                .success
                .value

              navigator
                .nextPageWithValueCheck(
                  ContactEmailPage,
                  CheckMode,
                  MDR,
                  updatedAnswers,
                  originalValue = Some("email@email.com")
                )
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from Contact Email page to 'Can we contact person by telephone' page " +
        "if user has changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactEmailPage, "email@email.com")
                .success
                .value

              navigator
                .nextPageWithValueCheck(
                  ContactEmailPage,
                  CheckMode,
                  MDR,
                  updatedAnswers,
                  originalValue = Some("originalemail@email.com")
                )
                .mustBe(routes.IsContactTelephoneController.onPageLoad(CheckMode, MDR))
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
              .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.ContactPhoneController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from IsContactTelephone page to 'is there someone else we can contact' page if NO is selected " +
        "and user registering as business with ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value
                  .set(BusinessTypePage, BusinessType.LimitedPartnership)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
                .mustBe(routes.SecondContactController.onPageLoad(CheckMode, MDR))
          }
        }

      "must go from IsContactTelephone page to CheckYourAnswers page if NO is selected " +
        "and user registering as individual without ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                  .success
                  .value
                  .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from IsContactTelephone page to CheckYourAnswers page if NO is selected " +
        "and user registering as individual with ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value
                  .set(BusinessTypePage, BusinessType.Sole)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from IsContactTelephone page to 'is there someone else we can contact' page if NO is selected " +
        "and user registering as business without ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                  .success
                  .value
                  .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
                .mustBe(routes.SecondContactController.onPageLoad(CheckMode, MDR))
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
              .nextPageWithValueCheck(SecondContactPage, NormalMode, MDR, updatedAnswers, None)
              .mustBe(routes.SndContactNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from Second Contact page to CheckYourAnswers page if true is selected " +
        "if user has NOT changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondContactPage, true)
                  .success
                  .value

              navigator
                .nextPageWithValueCheck(SecondContactPage, CheckMode, MDR, updatedAnswers, Some(true))
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
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
              .nextPageWithValueCheck(SecondContactPage, CheckMode, MDR, updatedAnswers, None)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Second Contact Name page to CheckYourAnswers page " +
        "if user has NOT changed their answer " in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactNamePage, "someName")
                  .success
                  .value

              navigator
                .nextPageWithValueCheck(SndContactNamePage, CheckMode, MDR, updatedAnswers, Some("someName"))
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from Second Contact Name page to Second Contact Email page " +
        "if user has changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactNamePage, "someName")
                  .success
                  .value

              navigator
                .nextPageWithValueCheck(SndContactNamePage, CheckMode, MDR, updatedAnswers, Some("originalName"))
                .mustBe(routes.SndContactEmailController.onPageLoad(CheckMode, MDR))
          }
        }

      "must go from Second Contact Email page to CheckYourAnswers page " +
        "if user has NOT changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactEmailPage, "email@email.com")
                  .success
                  .value

              navigator
                .nextPageWithValueCheck(SndContactEmailPage, CheckMode, MDR, updatedAnswers, Some("email@email.com"))
                .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
          }
        }

      "must go from Second Contact Email page to Second Contact have phone page " +
        "if user has changed their answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactEmailPage, "email@email.com")
                  .success
                  .value

              navigator
                .nextPageWithValueCheck(SndContactEmailPage, CheckMode, MDR, updatedAnswers, Some("original@email.com"))
                .mustBe(routes.SndConHavePhoneController.onPageLoad(CheckMode, MDR))
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
              .nextPage(SndConHavePhonePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.SndContactPhoneController.onPageLoad(CheckMode, MDR))
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
              .nextPage(SndConHavePhonePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }
    }
  }
}
