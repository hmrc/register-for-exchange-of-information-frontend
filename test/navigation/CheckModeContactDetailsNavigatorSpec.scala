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
import models.{CheckMode, MDR, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckModeContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {

    "in Check mode" - {

      "must go from Contact Name page to CheckYourAnswers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactNamePage, CheckMode, MDR, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Contact Email page to CheckYourAnswers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactEmailPage, CheckMode, MDR, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
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

      "must go from IsContactTelephone page to CheckYourAnswers page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(IsContactTelephonePage, false)
                .success
                .value

            navigator
              .nextPage(IsContactTelephonePage, CheckMode, MDR, updatedAnswers)
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
              .nextPage(SecondContactPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Second Contact Name page to CheckYourAnswers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SndContactNamePage, CheckMode, MDR, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from Second Contact Email page to CheckYourAnswers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SndContactEmailPage, CheckMode, MDR, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
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
