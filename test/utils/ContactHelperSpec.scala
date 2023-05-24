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

package utils

import base.SpecBase
import pages.{ContactNamePage, SndContactNamePage}
import play.api.test.Helpers

class ContactHelperSpec extends SpecBase with ContactHelper {

  "ContactHelper" - {

    "getFirstContactName" - {

      "must return the first contact name if it exists" in {
        val userAnswers = emptyUserAnswers.set(ContactNamePage, "Name").success.value
        val result      = getFirstContactName(userAnswers)(Helpers.stubMessages())

        result mustBe "Name"
      }

      "must return a default first contact name if one doesn't exist" in {
        val result = getFirstContactName(emptyUserAnswers)(Helpers.stubMessages())

        result mustBe "default.firstContact.name"
      }
    }

    "getSecondContactName" - {

      "must return the first contact name if it exists" in {
        val userAnswers = emptyUserAnswers.set(SndContactNamePage, "Name").success.value
        val result      = getSecondContactName(userAnswers)(Helpers.stubMessages())

        result mustBe "Name"
      }

      "must return a default first contact name if one doesn't exist" in {
        val result = getSecondContactName(emptyUserAnswers)(Helpers.stubMessages())

        result mustBe "default.secondContact.name"
      }
    }
  }
}
