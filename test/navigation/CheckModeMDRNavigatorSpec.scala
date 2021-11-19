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
import models.BusinessType.LimitedCompany
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.{CheckMode, MDR, Name, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class CheckModeMDRNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: MDRNavigator = new MDRNavigator

  "MDRNavigator" - {

    "in Check mode" - {

      "must go from DoYouHaveUniqueTaxPayerReference to CheckYourAnswers page if WhatAreYouRegisteringAsPage is set" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value
                .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(MDR))
        }
      }

      "must go from DoYouHaveUniqueTaxPayerReference to WhatAreYouRegisteringAs page if false is set" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUniqueTaxPayerReferencePage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUniqueTaxPayerReferencePage, CheckMode, MDR, updatedAnswers)
              .mustBe(routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode, MDR))
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
    }
  }
}
