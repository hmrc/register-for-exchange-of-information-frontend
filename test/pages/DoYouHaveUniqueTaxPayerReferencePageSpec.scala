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

package pages

import models.BusinessType.LimitedCompany
import models.{Name, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class DoYouHaveUniqueTaxPayerReferencePageSpec extends PageBehaviours {

  "DoYouHaveUniqueTaxPayerReferencePage" - {

    beRetrievable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beSettable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)

    beRemovable[Boolean](DoYouHaveUniqueTaxPayerReferencePage)
  }

  val date = LocalDate.now()
  "must remove pages ahead when user changes this page and check previous is required" in {
    forAll(arbitrary[UserAnswers]) {
      answers =>
        val firstResult = answers
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(BusinessTypePage, LimitedCompany)
          .success
          .value
          .set(UTRPage, "UTR")
          .success
          .value
          .set(WhatIsYourNamePage, Name("First", "Last"))
          .success
          .value
          .set(WhatIsYourDateOfBirthPage, date)
          .success
          .value
          .set(BusinessNamePage, "name")
          .success
          .value

        val cleanResult = firstResult
          .set(DoYouHaveUniqueTaxPayerReferencePage, false, true)
          .success
          .value

        cleanResult.get(BusinessTypePage) mustBe None
        cleanResult.get(UTRPage) mustBe None
        cleanResult.get(WhatIsYourNamePage) mustBe None
        cleanResult.get(WhatIsYourDateOfBirthPage) mustBe None
        cleanResult.get(BusinessNamePage) mustBe None

        val sameResult = firstResult
          .set(DoYouHaveUniqueTaxPayerReferencePage, true, true)
          .success
          .value

        sameResult.get(BusinessTypePage) mustBe Some(LimitedCompany)
        sameResult.get(UTRPage) mustBe Some("UTR")
        sameResult.get(WhatIsYourNamePage) mustBe Some(Name("First", "Last"))
        sameResult.get(WhatIsYourDateOfBirthPage) mustBe Some(date)
        sameResult.get(BusinessNamePage) mustBe Some("name")
    }
  }

}
