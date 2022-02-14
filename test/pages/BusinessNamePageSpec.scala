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

package pages

import models.{Name, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class BusinessNamePageSpec extends PageBehaviours {

  "BusinessNamePage" - {

    beRetrievable[String](BusinessNamePage)

    beSettable[String](BusinessNamePage)

    beRemovable[String](BusinessNamePage)
  }

  "cleanup" - {

    "must remove sole trader name when user enters a business name" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(SoleNamePage, Name("Sole", "Trader"))
            .success
            .value
            .set(BusinessNamePage, "Organisation")
            .success
            .value

          result.get(SoleNamePage) must not be defined
      }
    }
  }
}
