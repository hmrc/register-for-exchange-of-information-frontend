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

import models.{Name, UserAnswers}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.util.Try

object PageLists {

  val removePage: (Try[UserAnswers], QuestionPage[_]) => Try[UserAnswers] =
    (ua: Try[UserAnswers], page: QuestionPage[_]) =>
      ua.flatMap(
        x => x.remove(page)
      )

  val allAfterIsContactTelephonePages = List(ContactPhonePage)

  val allAfterSecondContactPages = List(SndContactNamePage, SndContactEmailPage, SndConHavePhonePage, SndContactPhonePage)

  val afterAllSndConHavePhonePages = List(SndContactPhonePage)

  val allAfterBusinessTypePage = List(UTRPage, BusinessNamePage, SoleNamePage, WhatIsYourDateOfBirthPage, SoleDateOfBirthPage)

  val individualWithIdJourney = List(WhatIsYourNationalInsuranceNumberPage, WhatIsYourNamePage, WhatIsYourDateOfBirthPage)

  val businessWithIdJourney = BusinessTypePage +: allAfterBusinessTypePage

  val individualWithoutIdJourney =
    List(BusinessWithoutIDNamePage, WhatIsYourDateOfBirthPage, DoYouLiveInTheUKPage, WhatIsYourPostcodePage, AddressUKPage, SelectAddressPage)

  val businessWithoutIdJourney = List()
}
