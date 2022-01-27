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
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class NormalModeMDRNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: MDRNavigator = new MDRNavigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from 'Do You Have Unique Tax Payer Reference?' page to 'What Are You Registering As?' page if NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatAreYouRegisteringAsController.onPageLoad(NormalMode, MDR))
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
              .nextPage(WhatAreYouRegisteringAsPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.DoYouHaveNINController.onPageLoad(NormalMode, MDR))
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
              .nextPage(DoYouHaveNINPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.NonUkNameController.onPageLoad(NormalMode, MDR))
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
              .nextPage(DoYouHaveNINPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(NormalMode, MDR))
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
              .nextPage(WhatIsYourNationalInsuranceNumberPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourNameController.onPageLoad(NormalMode, MDR))
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
              .nextPage(WhatIsYourNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode, MDR))
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
              .nextPage(NonUkNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode, MDR))
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
              .nextPage(WhatIsYourDateOfBirthPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'What Is Your DOB?' page to 'We could not confirm your identity' page when valid DOB is entered " +
        "but individual could not be matched" in {
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
                .nextPage(WhatIsYourDateOfBirthPage, NormalMode, MDR, updatedAnswers)
                .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode, MDR))
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
              .nextPage(DoYouLiveInTheUKPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.AddressWithoutIdController.onPageLoad(NormalMode, MDR))
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
                .nextPage(AddressWithoutIdPage, NormalMode, MDR, updatedAnswers)
                .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode, MDR))
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
                .nextPage(AddressWithoutIdPage, NormalMode, MDR, updatedAnswers)
                .mustBe(routes.ContactNameController.onPageLoad(NormalMode, MDR))
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
              .nextPage(DoYouLiveInTheUKPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsYourPostcodeController.onPageLoad(NormalMode, MDR))
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
              .nextPage(WhatIsYourPostcodePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SelectAddressController.onPageLoad(NormalMode, MDR))
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
              .nextPage(SelectAddressPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode, MDR))
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
              .nextPage(AddressUKPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'What are you registering as' page to 'What is the name of your business' page when business is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
                .success
                .value

            navigator
              .nextPage(WhatAreYouRegisteringAsPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.BusinessWithoutIDNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'What is the name of your business' page to 'Does your business trade under a different name' page when a valid business name is entered" in {
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
              .nextPage(BusinessWithoutIDNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.BusinessHaveDifferentNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Does your business trade under a different name' page to 'What is the trading name of your business' page when yes is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessHaveDifferentNamePage, true)
                .success
                .value

            navigator
              .nextPage(BusinessHaveDifferentNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.WhatIsTradingNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Does your business trade under a different name' page to 'What is the main address of your business' page when no is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessHaveDifferentNamePage, false)
                .success
                .value

            navigator
              .nextPage(BusinessHaveDifferentNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.AddressWithoutIdController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Do You Have UTR?' page to 'What is your business type?' page if YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.BusinessTypeController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'What is your business type?' page to 'UTR?' page when business type is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.Sole)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.UTRController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'UTR?' page to 'What is your name?' page when sole proprietor business type is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(UTRPage, UniqueTaxpayerReference("0123456789"))
                .success
                .value
                .set(BusinessTypePage, BusinessType.Sole)
                .success
                .value

            navigator
              .nextPage(UTRPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.SoleNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'UTR?' page to 'What is your business name?' page when NOT sole proprietor business type is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(UTRPage, UniqueTaxpayerReference("0123456789"))
                .success
                .value
                .set(BusinessTypePage, BusinessType.LimitedCompany)
                .success
                .value

            navigator
              .nextPage(UTRPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.BusinessNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'What is your name?' page for sole proprietor business to 'Is this your business?' page" in {
        val firstName: String = "First Name"
        val lastName: String  = "Last"
        val validAnswer: Name = Name(firstName, lastName)

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SoleNamePage, validAnswer)
                .success
                .value

            navigator
              .nextPage(SoleNamePage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.IsThisYourBusinessController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Is this your business' page to ' What is the email address for [contact name]' page when sole proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, Sole)
                .success
                .value
                .set(IsThisYourBusinessPage, true)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Is this your business' page to 'Who can we contact?'' page when any business other than Sole Proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, LimitedCompany)
                .success
                .value
                .set(IsThisYourBusinessPage, true)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.ContactNameController.onPageLoad(NormalMode, MDR))
        }
      }

      "must go from 'Is this your business' page to 'no-records-matched'' page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, LimitedCompany)
                .success
                .value
                .set(IsThisYourBusinessPage, false)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, MDR, updatedAnswers)
              .mustBe(routes.BusinessNotIdentifiedController.onPageLoad(MDR))
        }
      }
    }
  }
}
