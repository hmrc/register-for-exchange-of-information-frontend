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
import models.{Address, CheckMode, Country, MDR, UserAnswers, WhatAreYouRegisteringAs}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckModeMDRNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: MDRNavigator = new MDRNavigator

  "Navigator" - {

    "in Check mode" - {

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'CheckYourAnswers' page if user has not changed answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(DoYouHaveUniqueTaxPayerReferencePage, false)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers, oldValue = Some(false))
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What Are You Registering As?' page if user has changed answer to 'NO' " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(DoYouHaveUniqueTaxPayerReferencePage, false)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers, oldValue = Some(true))
              .mustBe(routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What type of business do you have?' page if user has changed answer to 'YES' " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(DoYouHaveUniqueTaxPayerReferencePage, true)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers, oldValue = Some(false))
              .mustBe(routes.BusinessTypeController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'Check Your Answers' page if user has not changed answer " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              .success
              .value

            navigator
              .nextPage(
                page = WhatAreYouRegisteringAsPage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers,
                oldValue = Some(WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'Do You Have NINO?' page if user has changed answer to 'Individual' " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              .success
              .value

            navigator
              .nextPage(page = WhatAreYouRegisteringAsPage,
                        mode = CheckMode,
                        regime = MDR,
                        userAnswers = updatedAnswers,
                        oldValue = Some(WhatAreYouRegisteringAs.RegistrationTypeBusiness)
              )
              .mustBe(routes.DoYouHaveNINController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'What is the name of your business' page if user has changed answer to 'Business' " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
              .success
              .value

            navigator
              .nextPage(
                page = WhatAreYouRegisteringAsPage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers,
                oldValue = Some(WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              )
              .mustBe(routes.BusinessWithoutIDNameController.onPageLoad(CheckMode, MDR))
        }
      }
    }

    "must go from 'What is the name of your business' page to 'Check Your Answers' page if user has not changed answer" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val updatedAnswers = answers
            .set(BusinessWithoutIDNamePage, "a business")
            .success
            .value

          navigator
            .nextPage(
              page = BusinessWithoutIDNamePage,
              mode = CheckMode,
              regime = MDR,
              userAnswers = updatedAnswers,
              oldValue = Some("a business")
            )
            .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
      }
    }
  }

  "must go from 'What is the name of your business?' page to 'Does your business trade under a different name?' page if user has changed answer" in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val updatedAnswers = answers
          .set(BusinessWithoutIDNamePage, "a new business")
          .success
          .value

        navigator
          .nextPage(
            page = BusinessWithoutIDNamePage,
            mode = CheckMode,
            regime = MDR,
            userAnswers = updatedAnswers,
            oldValue = Some("a business")
          )
          .mustBe(routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, MDR))
    }
  }

  "must go from 'Does your business trade under a different name?' page to 'Check Your Answers' page if user has selected 'NO' " in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val updatedAnswers = answers
          .set(BusinessHaveDifferentNamePage, false)
          .success
          .value

        navigator
          .nextPage(
            page = BusinessHaveDifferentNamePage,
            mode = CheckMode,
            regime = MDR,
            userAnswers = updatedAnswers
          )
          .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
    }
  }

  "must go from 'Does your business trade under a different name?' page to 'What is the trading name of your business?' page if user has selected 'YES' " in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val updatedAnswers = answers
          .set(BusinessHaveDifferentNamePage, true)
          .success
          .value

        navigator
          .nextPage(
            page = BusinessHaveDifferentNamePage,
            mode = CheckMode,
            regime = MDR,
            userAnswers = updatedAnswers
          )
          .mustBe(routes.WhatIsTradingNameController.onPageLoad(CheckMode, MDR))
    }
  }

  "must go from 'What is the trading name of your business?' page to 'Check Your Answers' page when user enters a name" in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val updatedAnswers = answers
          .set(WhatIsTradingNamePage, "tradeName")
          .success
          .value

        navigator
          .nextPage(
            page = WhatIsTradingNamePage,
            mode = CheckMode,
            regime = MDR,
            userAnswers = updatedAnswers
          )
          .mustBe(routes.AddressWithoutIdController.onPageLoad(CheckMode, MDR))
    }
  }

  "must go from 'What is the main address of your business' page to 'Check Your Answers' page when user enters address" in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val updatedAnswers = answers
          .set(AddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
          .success
          .value

        navigator
          .nextPage(
            page = AddressWithoutIdPage,
            mode = CheckMode,
            regime = MDR,
            userAnswers = updatedAnswers
          )
          .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
    }
  }

}
