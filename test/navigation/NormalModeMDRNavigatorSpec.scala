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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NormalModeMDRNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: MDRNavigator = new MDRNavigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What Are You Registering As?' page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatAreYouRegisteringAsController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What Are You Registering As' page to 'Do You Have NINO?' page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                .success
                .value

            navigator
              .nextPage(WhatAreYouRegisteringAsPage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouHaveNINController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Do You Have NINO?' page to 'What Is Your Name?' page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouHaveNINController.onPageLoad(NormalMode))
        }
      }

//      "must go from IsContactTelephone page to Second Contact Phone page if NO is selected" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            val updatedAnswers =
//              answers
//                .set(IsContactTelephonePage, false)
//                .success
//                .value
//
//            navigator
//              .nextPage(IsContactTelephonePage, NormalMode, updatedAnswers)
//              .mustBe(routes.SecondContactController.onPageLoad(NormalMode))
//        }
//      }
//
//      "must go from Second Contact page to Second Contact Name page if YES is selected" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            val updatedAnswers =
//              answers
//                .set(SecondContactPage, true)
//                .success
//                .value
//
//            navigator
//              .nextPage(SecondContactPage, NormalMode, updatedAnswers)
//              .mustBe(routes.SndContactNameController.onPageLoad(NormalMode))
//        }
//      }
//
//      "must go from Second Contact page to CheckYourAnswers page if NO is selected" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            val updatedAnswers =
//              answers
//                .set(SecondContactPage, false)
//                .success
//                .value
//
//            navigator
//              .nextPage(SecondContactPage, NormalMode, updatedAnswers)
//              .mustBe(routes.CheckYourAnswersController.onPageLoad())
//        }
//      }
//
//      "must go from Second Contact Name page to Second Contact Email page" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            navigator
//              .nextPage(SndContactNamePage, NormalMode, answers)
//              .mustBe(routes.SndContactEmailController.onPageLoad(NormalMode))
//        }
//      }
//
//      "must go from Second Contact Email page to Second Contact Have Phone page" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            navigator
//              .nextPage(SndContactEmailPage, NormalMode, answers)
//              .mustBe(routes.SndConHavePhoneController.onPageLoad(NormalMode))
//        }
//      }
//
//      "must go from Second Contact Have Phone page to Second Contact Phone page if YES is selected" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            val updatedAnswers =
//              answers
//                .set(SndConHavePhonePage, true)
//                .success
//                .value
//
//            navigator
//              .nextPage(SndConHavePhonePage, NormalMode, updatedAnswers)
//              .mustBe(routes.SndContactPhoneController.onPageLoad(NormalMode))
//        }
//      }
//
//      "must go from Second Contact Have Phone page to CheckYourAnswers page if NO is selected" in {
//        forAll(arbitrary[UserAnswers]) {
//          answers =>
//            val updatedAnswers =
//              answers
//                .set(SndConHavePhonePage, false)
//                .success
//                .value
//
//            navigator
//              .nextPage(SndConHavePhonePage, NormalMode, updatedAnswers)
//              .mustBe(routes.CheckYourAnswersController.onPageLoad())
//        }
//      }
    }
  }
}
