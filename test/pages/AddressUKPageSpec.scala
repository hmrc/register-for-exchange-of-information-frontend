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

import models.{Address, Country, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddressUKPageSpec extends PageBehaviours {

  "AddressUKPage" - {

    beRetrievable[Address](AddressUKPage)

    beSettable[Address](AddressUKPage)

    beRemovable[Address](AddressUKPage)
  }

  "must remove selectAddressPage when a manual UK address is entered" in {

    forAll(arbitrary[UserAnswers]) {
      userAnswers =>
        val result = userAnswers
          .set(AddressUKPage, Address("", None, "", None, None, Country("", "", "")))
          .success
          .value

        result.get(SelectAddressPage) must not be defined
    }
  }
}
