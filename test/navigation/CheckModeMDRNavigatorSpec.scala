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
import models.ReporterType._
import models._
import models.matching.{IndRegistrationInfo, SafeId}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class CheckModeMDRNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator: MDRNavigator = new MDRNavigator
  val checkYourAnswers: Call  = Navigator.checkYourAnswers

  "Navigator" - {

    "in CheckMode mode" - {

      "must go from Reporter type page" - {

        "to do you have Nino page if Individual is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, Individual)
                  .success
                  .value

              navigator
                .nextPage(ReporterTypePage, CheckMode, updatedAnswers)
                .mustBe(routes.DoYouHaveNINController.onPageLoad(CheckMode))
          }
        }

        "to Registered address in UK page if any organisation type or sole trader is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val orgOrSoleTraderReporters = List(LimitedCompany, LimitedPartnership, Partnership, UnincorporatedAssociation, Sole)

              orgOrSoleTraderReporters.foreach {
                organisation =>
                  val updatedAnswers =
                    answers
                      .set(ReporterTypePage, organisation)
                      .success
                      .value

                  navigator
                    .nextPage(ReporterTypePage, CheckMode, updatedAnswers)
                    .mustBe(routes.RegisteredAddressInUKController.onPageLoad(CheckMode))
              }
          }
        }
      }

      "must go from Registered address in the UK page" - {

        "to UTR page if Yes is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(RegisteredAddressInUKPage, true)
                  .success
                  .value

              navigator
                .nextPage(RegisteredAddressInUKPage, CheckMode, updatedAnswers)
                .mustBe(routes.UTRController.onPageLoad(CheckMode))
          }
        }

        "to do you have UTR page if No is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(RegisteredAddressInUKPage, false)
                  .success
                  .value

              navigator
                .nextPage(RegisteredAddressInUKPage, CheckMode, updatedAnswers)
                .mustBe(routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode))
          }
        }
      }

      "must go from Do you have UTR page" - {

        "to UTR page if Yes is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value

              navigator
                .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, updatedAnswers)
                .mustBe(routes.UTRController.onPageLoad(CheckMode))
          }
        }

        "to business without ID name page if No is selected and reporter type is any organisation" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val organisationReporters = List(LimitedCompany, LimitedPartnership, Partnership, UnincorporatedAssociation)

              organisationReporters.foreach {
                organisation =>
                  val updatedAnswers =
                    answers
                      .set(ReporterTypePage, organisation)
                      .success
                      .value
                      .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                      .success
                      .value

                  navigator
                    .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, updatedAnswers)
                    .mustBe(routes.BusinessWithoutIDNameController.onPageLoad(CheckMode))
              }
          }
        }

        "to do you have NINO page if No is selected and reporter type is Sole Trader" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, Sole)
                  .success
                  .value
                  .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                  .success
                  .value

              navigator
                .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, updatedAnswers)
                .mustBe(routes.DoYouHaveNINController.onPageLoad(CheckMode))
          }
        }
      }

      "must go from 'Do you Have a National Insurance Number' page to " - {

        "'WhatIsYourNationalInsuranceNumber' page if user has changed answer to 'Yes' " in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(DoYouHaveNINPage, false)
                .success
                .value
                .set(DoYouHaveNINPage, true)
                .success
                .value

              navigator
                .nextPage(DoYouHaveNINPage, CheckMode, updatedAnswers)
                .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode))
          }
        }

        "'What Is Your Name?' page if NO is selected" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(DoYouHaveNINPage, true)
                .success
                .value
                .set(DoYouHaveNINPage, false)
                .success
                .value

              navigator
                .nextPage(DoYouHaveNINPage, CheckMode, updatedAnswers)
                .mustBe(routes.NonUkNameController.onPageLoad(CheckMode))
          }
        }
      }

      "must go from 'What is your Unique Tax Payer Reference' (utr) page to " - {

        "'What is your Name' page if the business type is 'Sole trader'" in {

          forAll(arbitrary[UserAnswers], arbitraryUniqueTaxpayerReference.arbitrary) {
            (answers, utr) =>
              val updatedAnswers = answers
                .set(ReporterTypePage, Sole)
                .success
                .value
                .set(UTRPage, utr)
                .success
                .value

              navigator
                .nextPage(UTRPage, CheckMode, updatedAnswers)
                .mustBe(routes.SoleNameController.onPageLoad(CheckMode))
          }
        }

        "'What is your {business} Name' page if the reporter type is any organisation other than 'Sole trader'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val organisationReporters = List(LimitedCompany, LimitedPartnership, Partnership, UnincorporatedAssociation)

              organisationReporters.foreach {
                organisation =>
                  val updatedAnswers = answers
                    .set(ReporterTypePage, organisation)
                    .success
                    .value
                    .set(UTRPage, utr)
                    .success
                    .value

                  navigator
                    .nextPage(UTRPage, CheckMode, updatedAnswers)
                    .mustBe(routes.BusinessNameController.onPageLoad(CheckMode))
              }
          }
        }
      }

      "must go from 'WhatIsYourNationalInsuranceNumber' page to " - {

        "'What is your name?' page if valid NINO is entered AND 'What is your name?' page " in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(WhatIsYourNationalInsuranceNumberPage, Nino("CC123456C"))
                  .success
                  .value

              navigator
                .nextPage(WhatIsYourNationalInsuranceNumberPage, CheckMode, updatedAnswers)
                .mustBe(routes.WhatIsYourNameController.onPageLoad(CheckMode))
          }
        }
      }

      "must go from 'WhatIsYourName' page to 'WhatIsYourDateOfBirth' page when valid name is entered" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(WhatIsYourNamePage, Name("name", "surname"))
                .success
                .value

            navigator
              .nextPage(WhatIsYourNamePage, CheckMode, updatedAnswers)
              .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode))
        }
      }
    }

    "must go from 'What is the name of your business' page to " - {

      "'Check Your Answers' page if user has not changed answer & next page in Journey has an answer" in {

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
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad())

        }
      }
    }

    "must go from 'Does your business trade under a different name?' page to " - {

      "'Check Your Answers' page if user has selected 'NO' & next page in Journey has an answer " in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(BusinessHaveDifferentNamePage, false)
              .success
              .value
              .set(BusinessAddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
              .success
              .value

            navigator
              .nextPage(
                page = BusinessHaveDifferentNamePage,
                mode = CheckMode,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "'What is the trading name of your business?' page if user has selected 'YES' " in {

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
                userAnswers = updatedAnswers
              )
              .mustBe(routes.WhatIsTradingNameController.onPageLoad(CheckMode))
        }
      }
    }

    "must go from 'What is the trading name of your business?' page to " - {

      "'Check Your Answers' page when user enters a name & next page in Journey has an answer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(WhatIsTradingNamePage, "tradeName")
              .success
              .value
              .set(ContactNamePage, "someName")
              .success
              .value
              .set(BusinessAddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
              .success
              .value

            navigator
              .nextPage(
                page = WhatIsTradingNamePage,
                mode = CheckMode,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

    }

    "must go from 'What is the main address of your business' page to 'Check Your Answers' page when user enters address " +
      "& answer for next page of journey exists" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(BusinessAddressWithoutIdPage, Address("", None, "", None, None, Country("", "", "")))
              .success
              .value
              .set(ContactNamePage, "someName")
              .success
              .value

            navigator
              .nextPage(
                page = BusinessAddressWithoutIdPage,
                mode = CheckMode,
                userAnswers = updatedAnswers
              )
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

    "must go from 'Do You Have NINO?' page to 'What Is Your Name?' page if NO is selected & next page in Journey has NO answer" in {
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
            .nextPage(DoYouHaveNINPage, CheckMode, updatedAnswers)
            .mustBe(routes.NonUkNameController.onPageLoad(CheckMode))
      }
    }

    "must go from 'Do You Have NINO?' page to 'What Is Your NINO?' page if YES is selected & next page in Journey has NO answer" in {
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
            .nextPage(DoYouHaveNINPage, CheckMode, updatedAnswers)
            .mustBe(routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode))
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
              .remove(WhatIsYourDateOfBirthPage)
              .success
              .value

          navigator
            .nextPage(WhatIsYourNamePage, CheckMode, updatedAnswers)
            .mustBe(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode))
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
            .nextPage(NonUkNamePage, CheckMode, updatedAnswers)
            .mustBe(routes.DateOfBirthWithoutIdController.onPageLoad(CheckMode))
      }
    }

    "must go from 'What Is Your DOB?' page to 'Do You Live in the UK?' page when valid DOB is entered" +
      "if next page in journey has NO value" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, false)
                .success
                .value
                .set(DateOfBirthWithoutIdPage, LocalDate.now())
                .success
                .value
                .remove(DoYouLiveInTheUKPage)
                .success
                .value

            navigator
              .nextPage(DateOfBirthWithoutIdPage, CheckMode, updatedAnswers)
              .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(CheckMode))
        }
      }

    "must go from 'What Is Your DOB?' page to 'Check Your Answers' page when valid DOB is entered" +
      "if next page in journey has SOME value" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveNINPage, false)
                .success
                .value
                .set(DateOfBirthWithoutIdPage, LocalDate.now())
                .success
                .value
                .set(DoYouLiveInTheUKPage, true)
                .success
                .value

            navigator
              .nextPage(DateOfBirthWithoutIdPage, CheckMode, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
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
              .nextPage(WhatIsYourDateOfBirthPage, CheckMode, updatedAnswers)
              .mustBe(routes.WeHaveConfirmedYourIdentityController.onPageLoad(CheckMode))
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
            .nextPage(DoYouLiveInTheUKPage, CheckMode, updatedAnswers)
            .mustBe(routes.IndividualAddressWithoutIdController.onPageLoad(CheckMode))
      }
    }

    "must go from RegistrationInfo page to 'What is your email address' page when valid Registration info is entered" in {

      val updatedAnswers =
        emptyUserAnswers
          .set(RegistrationInfoPage, IndRegistrationInfo(SafeId("safe")))
          .success
          .value

      navigator
        .nextPage(RegistrationInfoPage, CheckMode, updatedAnswers)
        .mustBe(routes.IndividualContactEmailController.onPageLoad(CheckMode))
    }

    "must go from SoleName page to IsThisYourBusiness page when sole trader name is entered" in {

      val updatedAnswers =
        emptyUserAnswers
          .set(SoleNamePage, Name("first", "last"))
          .success
          .value

      navigator
        .nextPage(SoleNamePage, CheckMode, updatedAnswers)
        .mustBe(routes.IsThisYourBusinessController.onPageLoad(CheckMode))
    }

    "must go from BusinessName page to IsThisYourBusiness page when valid business name is entered" in {

      val updatedAnswers =
        emptyUserAnswers
          .set(BusinessNamePage, "name")
          .success
          .value

      navigator
        .nextPage(BusinessNamePage, CheckMode, updatedAnswers)
        .mustBe(routes.IsThisYourBusinessController.onPageLoad(CheckMode))
    }

    "must go from ContactName page to ContactEmail page when valid contact name is entered" in {

      val updatedAnswers =
        emptyUserAnswers
          .set(ContactNamePage, "name")
          .success
          .value

      navigator
        .nextPage(ContactNamePage, CheckMode, updatedAnswers)
        .mustBe(routes.ContactEmailController.onPageLoad(CheckMode))
    }

    "must go from 'What is your home address?'(NON-UK) page to 'What is your email address' page when valid address entered" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val updatedAnswers =
            answers
              .set(IndividualAddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
              .success
              .value
              .remove(IndividualContactEmailPage)
              .success
              .value

          navigator
            .nextPage(IndividualAddressWithoutIdPage, CheckMode, updatedAnswers)
            .mustBe(routes.IndividualContactEmailController.onPageLoad(CheckMode))
      }
    }

    "must go from 'What is the main address of your business'(NON-UK) page to 'Your contact details' page when valid address entered" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val updatedAnswers =
            answers
              .remove(ContactNamePage)
              .success
              .value
              .set(BusinessAddressWithoutIdPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
              .success
              .value

          navigator
            .nextPage(BusinessAddressWithoutIdPage, CheckMode, updatedAnswers)
            .mustBe(routes.YourContactDetailsController.onPageLoad(CheckMode))
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
            .nextPage(WhatIsYourPostcodePage, CheckMode, updatedAnswers)
            .mustBe(routes.SelectAddressController.onPageLoad(CheckMode))
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
              .remove(IndividualContactEmailPage)
              .success
              .value

          navigator
            .nextPage(SelectAddressPage, CheckMode, updatedAnswers)
            .mustBe(routes.IndividualContactEmailController.onPageLoad(CheckMode))
      }
    }

    "must go from 'What is your address?' page to 'Check your answers' page when address is selected & contact details still exist" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val updatedAnswers =
            answers
              .set(SelectAddressPage, "Some Address")
              .success
              .value
              .set(IndividualContactEmailPage, "email@email.com")
              .success
              .value

          navigator
            .nextPage(SelectAddressPage, CheckMode, updatedAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
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
              .remove(IndividualContactEmailPage)
              .success
              .value

          navigator
            .nextPage(AddressUKPage, CheckMode, updatedAnswers)
            .mustBe(routes.IndividualContactEmailController.onPageLoad(CheckMode))
      }
    }

    "must go from 'What is your home address?'(UK) page to 'Check your answers' page when address is selected" +
      "& contact details still exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(AddressUKPage, Address("Jarrow", None, "Park", None, None, Country("", "GB", "United Kingdom")))
                .success
                .value
                .set(IndividualContactEmailPage, "email@email.com")
                .success
                .value

            navigator
              .nextPage(AddressUKPage, CheckMode, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }
  }

}
