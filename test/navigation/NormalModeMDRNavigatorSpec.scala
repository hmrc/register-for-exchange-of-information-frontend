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
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

import java.time.LocalDate

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
              .mustBe(routes.NonUkNameController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Do You Have NINO?' page to 'What Is Your NINO?' page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What Is Your NINO?' page to 'What is your name?' page if valid NINO is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNationalInsuranceNumberPage, "QQ123456C")
                .success
                .value

            navigator
              .nextPage(WhatIsYourNationalInsuranceNumberPage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourNameController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your name?'(UK) page to 'What is your DOB?' page if valid NINO is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNamePage, WhatIsYourName("Little", "Comets"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What Is Your Name?'(NON-UK) page to 'What Is Your DOB?' page when valid name is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(NonUkNamePage, NonUkName("FirstName", "Surname"))
                .success
                .value

            navigator
              .nextPage(NonUkNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What Is Your DOB?' page to 'Do You Live in the UK?' page when valid DOB is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, false)
                .success
                .value
                .set(WhatIsYourDateOfBirthPage, LocalDate.now())
                .success
                .value

            navigator
              .nextPage(WhatIsYourDateOfBirthPage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(NormalMode))
        }
      }

      //TODO - add this test when logic is added for individual matching
      "must go from 'What Is Your DOB?' page to 'We have confirmed your identity' page when valid DOB is entered " +
        "and individual could be matched" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveNINPage, true)
                  .success
                  .value
                  .set(WhatIsYourDateOfBirthPage, LocalDate.now())
                  .success
                  .value

              navigator
                .nextPage(WhatIsYourDateOfBirthPage, NormalMode, updatedAnswers)
                .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad())
          }
        }

      //TODO - add this test when logic is added for individual matching
      "must go from 'What Is Your DOB?' page to 'We could not confirm your identity' page when valid DOB is entered " +
        "but individual could not be matched" ignore {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveNINPage, true)
                  .success
                  .value
                  .set(WhatIsYourDateOfBirthPage, LocalDate.now())
                  .success
                  .value

              navigator
                .nextPage(WhatIsYourDateOfBirthPage, NormalMode, updatedAnswers)
                .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad())
          }
        }

      "must go from 'Do You Live in the UK?' page to 'What is your home address (Non UK)' page when NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouLiveInTheUKPage, false)
                .success
                .value

            navigator
              .nextPage(DoYouLiveInTheUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.AddressWithoutIdController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your home address?'(NON-UK) page to 'What is your email address' page when valid address entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(AddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value

            navigator
              .nextPage(AddressWithoutIdPage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Do You Live in the UK?' page to 'What is your postcode?' page selected when YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouLiveInTheUKPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouLiveInTheUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourPostcodeController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your postcode?' page to 'What is your address?' page when valid postCode entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourPostcodePage, "AA1 1AA")
                .success
                .value

            navigator
              .nextPage(WhatIsYourPostcodePage, NormalMode, updatedAnswers)
              .mustBe(routes.SelectAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your address?' page to 'What is your email address' page when address is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SelectAddressPage, "Some Address")
                .success
                .value

            navigator
              .nextPage(SelectAddressPage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your home address?'(UK) page to 'What is your email address' page when address is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(AddressUKPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value

            navigator
              .nextPage(AddressUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What are registering as' page to 'What is the name of your business' page when business is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
                .success
                .value

            navigator
              .nextPage(WhatAreYouRegisteringAsPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessWithoutIDNameController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is the name of you business' page to 'What is the main address of your business' page when a valid business name is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
                .success
                .value
                .set(BusinessWithoutIDNamePage, "a business")
                .success
                .value

            navigator
              .nextPage(BusinessWithoutIDNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.AddressWithoutIdController.onPageLoad(NormalMode))
        }
      }
    }
  }
}
