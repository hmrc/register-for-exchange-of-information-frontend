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

package viewmodels

import base.SpecBase
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.{Address, Country, Name, NonUkName}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import utils.CountryListFactory

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with GuiceOneAppPerSuite {

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  def messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages                      = messagesApi.preferred(fakeRequest)
  val countryListFactory: CountryListFactory           = app.injector.instanceOf[CountryListFactory]

  "CheckYourAnswersViewModel" - {

    "must return required rows for 'business-with-id' flow" in {
      val userAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactNamePage, "secondContactName")
        .success
        .value
        .set(SndContactEmailPage, "secondContactEmail")
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
        .set(SndContactPhonePage, "secondContactPhone")
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = true)

      result.size mustBe 3
      result.head.rows.size mustBe 1
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3

      result(2).sectionName mustBe "Second contact"
      result(2).rows.size mustBe 4
    }

    "must return required rows for 'business-without-id' flow" in {
      val businessAddress = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
        .success
        .value
        .set(ContactEmailPage, "hello")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(IsContactTelephonePage, false)
        .success
        .value
        .set(BusinessAddressWithoutIdPage, businessAddress)
        .success
        .value
        .set(SecondContactPage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = true)

      result.size mustBe 3
      result.head.rows.size mustBe 3
      result.head.sectionName mustBe "Business details"

      result(1).sectionName mustBe "First contact"
      result(1).rows.size mustBe 3

      result(2).sectionName mustBe "Second contact"
      result(2).rows.size mustBe 1
    }

    "must return required rows for 'individual-with-id' flow" in {
      val userAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(WhatIsYourNamePage, Name("name", "last"))
        .success
        .value
        .set(IndividualContactEmailPage, "email@email.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = false)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 4

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2

    }

    "must return required rows for 'individual-without-id' flow" in {
      val address = Address("", None, "", None, None, Country("valid", "GB", "United Kingdom"))
      val userAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, false)
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("a", "b"))
        .success
        .value
        .set(IndividualContactEmailPage, "test@gmail.com")
        .success
        .value
        .set(WhatIsYourDateOfBirthPage, LocalDate.now())
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value
        .set(IndividualAddressWithoutIdPage, address)
        .success
        .value
        .set(DoYouLiveInTheUKPage, false)
        .success
        .value

      val result: Seq[Section] = CheckYourAnswersViewModel.buildPages(userAnswers, countryListFactory, isBusiness = false)

      result.size mustBe 2

      result.head.sectionName mustBe "Your details"
      result.head.rows.size mustBe 6

      result(1).sectionName mustBe "Contact details"
      result(1).rows.size mustBe 2
    }
  }
}
