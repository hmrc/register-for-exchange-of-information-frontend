/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ReporterType.{LimitedCompany, Sole}
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
                .set(WhatIsYourNationalInsuranceNumberPage, Nino("CC123456C"))
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
                .set(WhatIsYourNamePage, Name("Little", "Comets"))
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
              .mustBe(routes.DateOfBirthWithoutIdController.onPageLoad(NormalMode))
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
                .nextPage(WhatIsYourDateOfBirthPage, NormalMode, updatedAnswers)
                .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode))
          }
        }

      "must go from DateOfBirthWithoutId page to 'Do you live in the UK' page when valid DOB is entered" in {

        val updatedAnswers =
          emptyUserAnswers
            .set(DoYouHaveNINPage, false)
            .success
            .value
            .set(DateOfBirthWithoutIdPage, LocalDate.now())
            .success
            .value

        navigator
          .nextPage(DateOfBirthWithoutIdPage, NormalMode, updatedAnswers)
          .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(NormalMode))
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
              .mustBe(routes.IndividualAddressWithoutIdController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your home address?'(NON-UK) page to " +
        "'What is your email address' page when valid address entered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(IndividualAddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                  .success
                  .value

              navigator
                .nextPage(IndividualAddressWithoutIdPage, NormalMode, updatedAnswers)
                .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode))
          }
        }

      "must go from 'What is the main address of your business'(NON-UK) page to " +
        "'Your contact details' page when valid address entered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(BusinessAddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                  .success
                  .value

              navigator
                .nextPage(BusinessAddressWithoutIdPage, NormalMode, updatedAnswers)
                .mustBe(routes.YourContactDetailsController.onPageLoad(NormalMode))
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
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode))
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
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is the name of your business' page to 'Does your business trade under a different name' page when a valid business name is entered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessWithoutIDNamePage, "a business")
                .success
                .value

            navigator
              .nextPage(BusinessWithoutIDNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessHaveDifferentNameController.onPageLoad(NormalMode))
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
              .nextPage(BusinessHaveDifferentNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsTradingNameController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is the trading name of your business' page to 'What is the main address of your business' page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(WhatIsTradingNamePage, NormalMode, answers)
              .mustBe(routes.BusinessAddressWithoutIdController.onPageLoad(NormalMode))
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
              .nextPage(BusinessHaveDifferentNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessAddressWithoutIdController.onPageLoad(NormalMode))
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
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, NormalMode, updatedAnswers)
              .mustBe(routes.ReporterTypeController.onPageLoad(NormalMode))
        }
      }

      "must go from 'What is your business type?' page to 'UTR?' page when business type is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(ReporterTypePage, ReporterType.Sole)
                .success
                .value

            navigator
              .nextPage(ReporterTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.UTRController.onPageLoad(NormalMode))
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
                .set(ReporterTypePage, ReporterType.Sole)
                .success
                .value

            navigator
              .nextPage(UTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.SoleNameController.onPageLoad(NormalMode))
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
                .set(ReporterTypePage, ReporterType.LimitedCompany)
                .success
                .value

            navigator
              .nextPage(UTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNameController.onPageLoad(NormalMode))
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
              .nextPage(SoleNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
        }
      }

      "must go from BusinessName page to 'Is this your business?' page" in {
        val businessName: String = "Business Name"

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessNamePage, businessName)
                .success
                .value

            navigator
              .nextPage(BusinessNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
        }
      }

      "must go from RegistrationInfo page to ' What is the email address for [contact name]'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(RegistrationInfoPage, NormalMode, answers)
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Is this your business' page to ' What is the email address for [contact name]' page when sole proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(ReporterTypePage, Sole)
                .success
                .value
                .set(IsThisYourBusinessPage, true)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
              .mustBe(routes.IndividualContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Is this your business' page to 'Your contact details?'' page when any business other than Sole Proprietor" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(ReporterTypePage, LimitedCompany)
                .success
                .value
                .set(IsThisYourBusinessPage, true)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
              .mustBe(routes.YourContactDetailsController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Is this your business' page to 'no-records-matched'' page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(ReporterTypePage, LimitedCompany)
                .success
                .value
                .set(IsThisYourBusinessPage, false)
                .success
                .value

            navigator
              .nextPage(IsThisYourBusinessPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNotIdentifiedController.onPageLoad())
        }
      }
    }
  }
}
