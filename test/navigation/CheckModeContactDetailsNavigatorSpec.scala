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
import models.{CheckMode, NormalMode, ReporterType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckModeContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Contact Name page to CheckYourAnswers page if user has changed their answer " +
        "& answer for next page of journey exists" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactNamePage, "someName")
                .success
                .value
                .set(ContactEmailPage, "email@email.com")
                .success
                .value

              navigator
                .nextPage(
                  ContactNamePage,
                  CheckMode,
                  updatedAnswers
                )
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from Contact Name page to Contact Email Address page if user has changed their answer " +
        "& answer for next page of journey does NOT exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactNamePage, "someName")
                .success
                .value
                .remove(ContactEmailPage)
                .success
                .value

              navigator
                .nextPage(
                  ContactNamePage,
                  CheckMode,
                  updatedAnswers
                )
                .mustBe(routes.ContactEmailController.onPageLoad(CheckMode))
          }
        }

      "must go from Contact Email page to CheckYourAnswers page if user has changed their answer " +
        "& answer for next page of journey exists" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactEmailPage, "email@email.com")
                .success
                .value
                .set(IsContactTelephonePage, true)
                .success
                .value

              navigator
                .nextPage(
                  ContactEmailPage,
                  CheckMode,
                  updatedAnswers
                )
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from Contact Email page to 'Can we contact person by telephone' page if user has changed their answer" +
        "& answer for next page of journey does NOT exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(ContactEmailPage, "email@email.com")
                .success
                .value
                .remove(IsContactTelephonePage)
                .success
                .value

              navigator
                .nextPage(
                  ContactEmailPage,
                  CheckMode,
                  updatedAnswers
                )
                .mustBe(routes.IsContactTelephoneController.onPageLoad(CheckMode))
          }
        }

      "must go from IndividualContactEmail page to IndividualHaveContactTelephone page when that page has not been previously completed " in {
        navigator
          .nextPage(IndividualContactEmailPage, CheckMode, emptyUserAnswers)
          .mustBe(routes.IndividualHaveContactTelephoneController.onPageLoad(CheckMode))

      }

      "must go from Contact Phone page to Second Contact page when second contact page has not been previously completed " in {
        navigator
          .nextPage(ContactPhonePage, CheckMode, emptyUserAnswers)
          .mustBe(routes.SecondContactController.onPageLoad(CheckMode))

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
              .nextPage(IsContactTelephonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from IsContactTelephone page to 'is there someone else we can contact' page if NO is selected " +
        "and user registering as business with ID and second contact page has NO value" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value
                  .set(ReporterTypePage, ReporterType.LimitedPartnership)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value
                  .remove(SecondContactPage)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, updatedAnswers)
                .mustBe(routes.SecondContactController.onPageLoad(CheckMode))
          }
        }

      "must go from IsContactTelephone page to 'is there someone else we can contact' page if NO is selected " +
        "and user registering as business with ID and second contact page has SOME value" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value
                  .set(ReporterTypePage, ReporterType.LimitedPartnership)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value
                  .set(SecondContactPage, true)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from 'Can we contact you by telephone?' page to CheckYourAnswers page if NO is selected " +
        "and user registering as individual without ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, ReporterType.Individual)
                  .success
                  .value
                  .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                  .success
                  .value
                  .set(IndividualHaveContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IndividualHaveContactTelephonePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from 'Can we contact you by telephone?' page to CheckYourAnswers page if NO is selected " +
        "and user registering as individual with ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveUniqueTaxPayerReferencePage, true)
                  .success
                  .value
                  .set(ReporterTypePage, ReporterType.Sole)
                  .success
                  .value
                  .set(IndividualHaveContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IndividualHaveContactTelephonePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from IsContactTelephone page to 'is there someone else we can contact' page if NO is selected " +
        "and user registering as business without ID" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ReporterTypePage, ReporterType.LimitedCompany)
                  .success
                  .value
                  .set(RegisteredAddressInUKPage, false)
                  .success
                  .value
                  .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                  .success
                  .value
                  .set(IsContactTelephonePage, false)
                  .success
                  .value

              navigator
                .nextPage(IsContactTelephonePage, CheckMode, updatedAnswers)
                .mustBe(routes.SecondContactController.onPageLoad(CheckMode))
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
              .nextPage(SecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.SndContactNameController.onPageLoad(NormalMode))
        }
      }

      "must go from Second Contact page to Second Contact Name page if true is selected " in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SecondContactPage, true)
                .success
                .value
                .remove(SndContactNamePage)
                .success
                .value

            navigator
              .nextPage(SecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.SndContactNameController.onPageLoad(CheckMode))
        }
      }

      "must go from Second Contact page to CheckYourAnswers page if YES is selected " +
        "and Second Contact Name page contains an answer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondContactPage, true)
                  .success
                  .value
                  .set(SndContactNamePage, "someName")
                  .success
                  .value

              navigator
                .nextPage(SecondContactPage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
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
              .nextPage(SecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from Second Contact Name page to CheckYourAnswers page if user has changed their answer " +
        "& answer for next page of journey exists" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactNamePage, "someName")
                  .success
                  .value
                  .set(SndContactEmailPage, "email@email.com")
                  .success
                  .value

              navigator
                .nextPage(SndContactNamePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from Second Contact Name page to Second Contact Email page if user has changed their answer " +
        "& answer for next page of journey does NOT exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactNamePage, "someName")
                  .success
                  .value
                  .remove(SndContactEmailPage)
                  .success
                  .value

              navigator
                .nextPage(SndContactNamePage, CheckMode, updatedAnswers)
                .mustBe(routes.SndContactEmailController.onPageLoad(CheckMode))
          }
        }

      "must go from Second Contact Email page to CheckYourAnswers page if user has changed their answer " +
        "& answer for next page of journey exists " in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactEmailPage, "email@email.com")
                  .success
                  .value
                  .set(SndConHavePhonePage, true)
                  .success
                  .value

              navigator
                .nextPage(SndContactEmailPage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from Second Contact Email page to Second Contact have phone page if user has changed their answer " +
        "& answer for next page of journey does NOT exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SndContactEmailPage, "email@email.com")
                  .success
                  .value
                  .remove(SndConHavePhonePage)
                  .success
                  .value

              navigator
                .nextPage(SndContactEmailPage, CheckMode, updatedAnswers)
                .mustBe(routes.SndConHavePhoneController.onPageLoad(CheckMode))
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
              .nextPage(SndConHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.SndContactPhoneController.onPageLoad(CheckMode))
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
              .nextPage(SndConHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }
    }
  }
}
