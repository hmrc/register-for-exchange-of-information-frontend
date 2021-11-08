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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class SndConHavePhonePageSpec extends PageBehaviours {

  "SndConHavePhonePage" - {

    beRetrievable[Boolean](SndConHavePhonePage)

    beSettable[Boolean](SndConHavePhonePage)

    beRemovable[Boolean](SndConHavePhonePage)

    "cleanup" - {

      "must remove SndContactPhonePage when there is a change of the answer to 'No'" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(SndContactPhonePage, "112233445566")
              .success
              .value
              .set(SndConHavePhonePage, false)
              .success
              .value

            result.get(ContactPhonePage) must not be defined
        }
      }

      "must retain SndContactPhonePage when there is a change of the answer to 'Yes'" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(SndContactPhonePage, "112233445566")
              .success
              .value
              .set(SndConHavePhonePage, true)
              .success
              .value

            result.get(ContactPhonePage) mustBe defined
        }
      }
    }
  }
}
