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

package models.subscription.request

import base.SpecBase
import generators.Generators
import models.{Name, NonUkName, ReporterType, UserAnswers}
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino

class ContactTypeSpec extends SpecBase with Generators with ScalaCheckPropertyChecks with EitherValues {

  "ContactInformation" - {
    "must serialise and de-serialise PrimaryContact" in {
      val json: JsValue =
        Json.parse("""
          |{"organisation":{"organisationName":"name"},"email":"test@t.com"}""".stripMargin)

      val primaryContact = ContactInformation(OrganisationDetails("name"), "test@t.com", None, None)
      Json.toJson(primaryContact) mustBe json
      json.as[ContactInformation] mustBe primaryContact
    }

    "must serialise and de-serialise SecondaryContact" in {
      val json: JsValue = Json.parse("""{"individual":{"firstName":"name","lastName":"last"},"email":"test@t.com"}""".stripMargin)

      val secondaryContact = ContactInformation(IndividualDetails("name", None, "last"), "test@t.com", None, None)
      Json.toJson(secondaryContact) mustBe json
      json.as[ContactInformation] mustBe secondaryContact
    }

    "must return PrimaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(ContactEmailPage, "test@test.com")
        .success
        .value
        .set(ContactNamePage, "Name Name")
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value

      ContactInformation.convertToPrimary(userAnswers).value mustBe ContactInformation(OrganisationDetails("Name Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'Individual with Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(WhatIsYourNationalInsuranceNumberPage, Nino("AA000000A"))
        .success
        .value
        .set(WhatIsYourNamePage, Name("Name", "Name"))
        .success
        .value
        .set(IndividualContactEmailPage, "test@test.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation.convertToPrimary(userAnswers).value mustBe ContactInformation(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'Individual without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, NonUkName("Name", "Name"))
        .success
        .value
        .set(IndividualContactEmailPage, "test@test.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation.convertToPrimary(userAnswers).value mustBe ContactInformation(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return PrimaryContact for the input 'UserAnswers with ReporterType as Sole trader'" in {
      val userAnswers = UserAnswers("id")
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(ReporterTypePage, ReporterType.Sole)
        .success
        .value
        .set(SoleNamePage, Name("Name", "Name"))
        .success
        .value
        .set(IndividualContactEmailPage, "test@test.com")
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation.convertToPrimary(userAnswers).value mustBe ContactInformation(IndividualDetails("Name", None, "Name"), "test@test.com", None, None)
    }

    "must return SecondaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = UserAnswers("id")
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, "test@test.com")
        .success
        .value
        .set(SecondContactPage, true)
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

      val expectedValue = ContactInformation(OrganisationDetails("Name Name"), "test@test.com", Some("11222244"), None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }

    "must return SecondaryContact for the input 'Business with/without Id UserAnswers' when SndConHavePhonePage is false" in {
      val userAnswers = UserAnswers("id")
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, "test@test.com")
        .success
        .value
        .set(SndContactNamePage, "Name Name")
        .success
        .value
        .set(SndConHavePhonePage, false)
        .success
        .value

      val expectedValue = ContactInformation(OrganisationDetails("Name Name"), "test@test.com", None, None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }

    "must return None when SecondContactPage is true and SndConHavePhonePage is true and SndContactPhonePage is empty" in {
      val userAnswers = UserAnswers("id")
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, "test@test.com")
        .success
        .value
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactNamePage, "Name Name")
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
        .set(SndContactPhonePage, "07540000000")
        .success
        .value

      val expectedValue = ContactInformation(OrganisationDetails("Name Name"), "test@test.com", Some("07540000000"), None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }
  }
}
