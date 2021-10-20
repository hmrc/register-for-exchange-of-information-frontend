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

package models.subscription.request

import base.SpecBase
import generators.Generators
import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.{BusinessType, Name, NonUkName, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsValue, Json}

class ContactInformationSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "ContactInformation" - {
    "must serialise and de-serialise PrimaryContact" in {
      val json: JsValue =
        Json.parse("""
          |{"organisation":{"organisationName":"name"},"email":"test@t.com"}""".stripMargin)

      val primaryContact = PrimaryContact(OrganisationDetails("name"), "test@t.com", None, None)
      Json.toJson(primaryContact) mustBe json
      json.as[PrimaryContact] mustBe primaryContact
    }

    "must serialise and de-serialise SecondaryContact" in {
      val json: JsValue = Json.parse("""{"individual":{"firstName":"name","lastName":"last"},"email":"test@t.com"}""".stripMargin)

      val secondaryContact = SecondaryContact(IndividualDetails("name", None, "last"), "test@t.com", None, None)
      Json.toJson(secondaryContact) mustBe json
      json.as[SecondaryContact] mustBe secondaryContact
    }

    "must return PrimaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value

      PrimaryContact.convertTo(userAnswers).value mustBe PrimaryContact(OrganisationDetails("Name Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'Individual with Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(WhatIsYourNamePage, Name("Name", "Name"))
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value

      PrimaryContact.convertTo(userAnswers).value mustBe PrimaryContact(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'Individual without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(NonUkNamePage, NonUkName("Name", "Name"))
        .success
        .value
        .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value

      PrimaryContact.convertTo(userAnswers).value mustBe PrimaryContact(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'UserAnswers with BusinessType as Sole trader' " in {
      val userAnswers = UserAnswers("id")
        .set(SoleNamePage, Name("Name", "Name"))
        .success
        .value
        .set(BusinessTypePage, BusinessType.Sole)
        .success
        .value
        .set(ContactEmailPage, "test@test.com")
        .success
        .value

      PrimaryContact.convertTo(userAnswers).value mustBe PrimaryContact(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return SecondaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(SndContactEmailPage, "test@test.com")
        .success
        .value
        .set(SndContactNamePage, "Name Name")
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
        .set(SndContactPhonePage, "11222244")
        .success
        .value

      SecondaryContact.convertTo(userAnswers).value mustBe SecondaryContact(OrganisationDetails("Name Name"), "test@test.com", Some("11222244"), None)
    }
  }
}
