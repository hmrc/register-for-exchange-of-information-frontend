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

package pages

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class BusinessHaveDifferentNamePageSpec extends PageBehaviours {

  "BusinessHaveDifferentNamePage" - {

    beRetrievable[Boolean](BusinessHaveDifferentNamePage)

    beSettable[Boolean](BusinessHaveDifferentNamePage)

    beRemovable[Boolean](BusinessHaveDifferentNamePage)

    "must remove business trading name when user changes answer from 'Yes' to 'No'" in {
      forAll(arbitrary[UserAnswers]) { answers =>
        val result = answers
          .set(WhatIsTradingNamePage, "differentName")
          .success
          .value
          .set(BusinessHaveDifferentNamePage, false)
          .success
          .value

        result.get(WhatIsTradingNamePage) must not be defined
      }
    }
  }
}
