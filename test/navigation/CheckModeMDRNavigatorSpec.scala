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
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class CheckModeMDRNavigator_Spec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: MDRNavigator = new MDRNavigator

  "Navigator" - {

    "in CheckMode mode" - {

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'CheckYourAnswers' page if user has not changed answer " +
        "& answer for next page of journey exists" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(DoYouHaveUniqueTaxPayerReferencePage, false)
              .success
              .value
              .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What Are You Registering As?' page if user has changed answer to 'NO' " +
        "& answer for next page of journey does NOT exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(DoYouHaveUniqueTaxPayerReferencePage, false)
              .success
              .value
              .remove(WhatAreYouRegisteringAsPage)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers)
              .mustBe(routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What type of business do you have?' page if user has changed answer to 'YES' " +
        "& answer for next page of journey does NOT exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setOrCleanup(DoYouHaveUniqueTaxPayerReferencePage, true)
              .success
              .value
              .remove(BusinessTypePage)
              .success
              .value

            navigator
              .nextPage(page = DoYouHaveUniqueTaxPayerReferencePage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers)
              .mustBe(routes.BusinessTypeController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'Check Your Answers' page if user has not changed answer " +
        "& answer for next page of journey exists" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              .success
              .value
              .set(DoYouHaveNINPage, true)
              .success
              .value

            navigator
              .nextPage(
                page = WhatAreYouRegisteringAsPage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'Do You Have NINO?' page if user has changed answer to 'Individual' " +
        "& answer for next page of journey does NOT exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
              .success
              .value
              .remove(DoYouHaveNINPage)
              .success
              .value

            navigator
              .nextPage(page = WhatAreYouRegisteringAsPage, mode = CheckMode, regime = MDR, userAnswers = updatedAnswers)
              .mustBe(routes.DoYouHaveNINController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What Are You Registering As' page to 'What is the name of your business' page if user has changed answer to 'Business' " +
        "& answer for next page of journey does NOT exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeBusiness)
              .success
              .value
              .remove(BusinessWithoutIDNamePage)
              .success
              .value

            navigator
              .nextPage(
                page = WhatAreYouRegisteringAsPage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.BusinessWithoutIDNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from DoYouHaveNIN page to CheckYourAnswers page if WhatIsYourNationalInsuranceNumberPage is set" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, true)
                .success
                .value
                .set(WhatIsYourNationalInsuranceNumberPage, Nino("AA000000A"))
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from DoYouHaveNIN page to WhatIsYourNationalInsuranceNumber page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode, MDR))
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
                .remove(NonUkNamePage)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.NonUkNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from WhatIsYourNationalInsuranceNumber page to CheckYourAnswers page if WhatIsYourNamePage is set" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNationalInsuranceNumberPage, Nino("AA000000A"))
                .success
                .value
                .set(WhatIsYourNamePage, Name("name", "surname"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNationalInsuranceNumberPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from WhatIsYourNationalInsuranceNumber page to WhatIsYourName page if value is provided" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNationalInsuranceNumberPage, Nino("AA000000A"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNationalInsuranceNumberPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from WhatIsYourName page to CheckYourAnswers page if WhatIsYourDateOfBirthPage is set" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNamePage, Name("name", "surname"))
                .success
                .value
                .set(WhatIsYourDateOfBirthPage, LocalDate.now())
                .success
                .value

            navigator
              .nextPage(WhatIsYourNamePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from WhatIsYourName page to WhatIsYourDateOfBirth page if value is provided" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNamePage, Name("name", "surname"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNamePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is the name of your business' page to 'Check Your Answers' page if user has not changed answer " +
        "& next page in Journey has an answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(BusinessWithoutIDNamePage, "a business")
              .success
              .value
              .set(BusinessHaveDifferentNamePage, true)
              .success
              .value

            navigator
              .nextPage(
                page = BusinessWithoutIDNamePage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))

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
                userAnswers = updatedAnswers
              )
              .mustBe(routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'Does your business trade under a different name?' page to 'Check Your Answers' page if user has selected 'NO' " +
        "& next page in Journey has an answer " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(BusinessHaveDifferentNamePage, false)
              .success
              .value
              .set(AddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
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

      "must go from 'What is the trading name of your business?' page to 'Check Your Answers' page when user enters a name " +
        "& next page in Journey has an answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatIsTradingNamePage, "tradeName")
              .success
              .value
              .set(ContactNamePage, "someName")
              .success
              .value
              .set(AddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
              .success
              .value

            navigator
              .nextPage(
                page = WhatIsTradingNamePage,
                mode = CheckMode,
                regime = MDR,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'What is the main address of your business' page to 'Check Your Answers' page when user enters address " +
        "& answer for next page of journey exists" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
              .success
              .value
              .set(AddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
              .success
              .value
              .set(ContactNamePage, "someName")
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

      "must go from 'Do You Have NINO?' page to 'What Is Your Name?' page if NO is selected " +
        "& next page in Journey has NO answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, false)
                .success
                .value
                .remove(NonUkNamePage)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.NonUkNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'Do You Have NINO?' page to 'What Is Your NINO?' page if YES is selected " +
        "& next page in Journey has NO answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, true)
                .success
                .value
                .remove(WhatIsYourNationalInsuranceNumberPage)
                .success
                .value

            navigator
              .nextPage(DoYouHaveNINPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What Is Your NINO?' page to 'What is your name?' page if valid NINO is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNationalInsuranceNumberPage, Nino("CC123456C"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNationalInsuranceNumberPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNameController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is your name?'(UK) page to 'What is your DOB?' page if valid NINO is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNamePage, Name("Little", "Comets"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNamePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, MDR))
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
              .nextPage(NonUkNamePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, MDR))
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
              .nextPage(WhatIsYourDateOfBirthPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, MDR))
        }
      }

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
              .nextPage(WhatIsYourDateOfBirthPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad(CheckMode, MDR))
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
              .nextPage(DoYouLiveInTheUKPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.AddressWithoutIdController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is your home address?'(NON-UK) page to " +
        "'What is your email address' page when valid address entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
                .success
                .value
                .set(AddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value

            navigator
              .nextPage(AddressWithoutIdPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is the main address of your business'(NON-UK) page to " +
        "'Who can we contact?' page when valid address entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
                .success
                .value
                .set(AddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value

            navigator
              .nextPage(AddressWithoutIdPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.ContactNameController.onPageLoad(CheckMode, MDR))
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
              .nextPage(WhatIsYourPostcodePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.SelectAddressController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is your address?' page to 'What is your email address' page when address is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                .success
                .value
                .set(SelectAddressPage, "Some Address")
                .success
                .value

            navigator
              .nextPage(SelectAddressPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is your address?' page to 'Check your answers' page when address is selected" +
        "& contact details still exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                .success
                .value
                .set(SelectAddressPage, "Some Address")
                .success
                .value
                .set(ContactEmailPage, "email@email.com")
                .success
                .value

            navigator
              .nextPage(SelectAddressPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from 'What is your home address?'(UK) page to 'What is your email address' page when address is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                .success
                .value
                .set(AddressUKPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value

            navigator
              .nextPage(AddressUKPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.ContactEmailController.onPageLoad(CheckMode, MDR))
        }
      }

      "must go from 'What is your home address?'(UK) page to 'Check your answers' page when address is selected" +
        "& contact details still exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, WhatAreYouRegisteringAs.RegistrationTypeIndividual)
                .success
                .value
                .set(AddressUKPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value
                .set(ContactEmailPage, "email@email.com")
                .success
                .value

            navigator
              .nextPage(AddressUKPage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }
    }
  }
}
