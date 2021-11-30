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

package utils

import base.SpecBase
import config.FrontendAppConfig
import models.{MDR, UserAnswers}
import pages.{ContactEmailPage, ContactNamePage, ContactPhonePage}
import play.api.Environment

class CheckYourAnswersViewModelSpec extends SpecBase {
  val conf: FrontendAppConfig = mock[FrontendAppConfig]
  val env                     = mock[Environment]
  val countryListFactory      = new CountryListFactory(env, conf)

  // first contact
  val firstContactName    = "first-contact-name"
  val firstContactEmail   = "first-contact-email"
  val isFirstContactPhone = true
  val firstContactPhone   = "+44 0808 157 0192"

  // second contact
  val isSecondContact      = true
  val secondContactName    = "second-contact-name"
  val secondContactEmail   = "second-contact-email"
  val isSecondContactPhone = true
  val secondContactPhone   = "+44 0808 157 0193"

  val userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(ContactNamePage, firstContactName)
    .success
    .value
    .set(ContactEmailPage, firstContactEmail)
    .success
    .value
    .set(ContactPhonePage, firstContactPhone)
    .success
    .value

  // build all
  //val helper = new CheckYourAnswersHelper(userAnswers, MDR, countryListFactory = countryListFactory)
}
