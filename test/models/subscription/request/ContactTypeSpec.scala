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
import models.ReporterType
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino

class ContactTypeSpec extends SpecBase with Generators with ScalaCheckPropertyChecks with EitherValues {

  "ContactInformation" - {
    "must serialise and de-serialise PrimaryContact" in {
      val json: JsValue =
        Json.parse(s"""
          |{"organisation":{"organisationName":"$OrgName"},"email":"$TestEmail"}""".stripMargin)

      val primaryContact = ContactInformation(OrganisationDetails(OrgName), TestEmail, None, None)
      Json.toJson(primaryContact) mustBe json
      json.as[ContactInformation] mustBe primaryContact
    }

    "must serialise and de-serialise SecondaryContact" in {
      val json: JsValue = Json.parse(
        s"""{"$IndividualKey":{"firstName":"${name.firstName}","lastName":"${name.lastName}"},"email":"$TestEmail"}""".stripMargin
      )

      val secondaryContact =
        ContactInformation(IndividualDetails(name.firstName, None, name.lastName), TestEmail, None, None)
      Json.toJson(secondaryContact) mustBe json
      json.as[ContactInformation] mustBe secondaryContact
    }

    "must return PrimaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = emptyUserAnswers
        .set(ContactEmailPage, TestEmail)
        .success
        .value
        .set(ContactNamePage, name.fullName)
        .success
        .value
        .set(ContactHavePhonePage, false)
        .success
        .value

      ContactInformation.convertToPrimary(userAnswers).value mustBe ContactInformation(
        OrganisationDetails(name.fullName),
        TestEmail,
        None,
        None
      )
    }

    "must return PrimaryContact for the input 'Individual with Id UserAnswers' " in {
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(DoYouHaveNINPage, true)
        .success
        .value
        .set(WhatIsYourNationalInsuranceNumberPage, Nino(TestNiNumber))
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(IndividualContactEmailPage, TestEmail)
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation
        .convertToPrimary(userAnswers)
        .value mustBe ContactInformation(IndividualDetails(name.firstName, None, name.lastName), TestEmail, None, None)
    }

    "must return PrimaryContact for the input 'Individual without Id UserAnswers' " in {
      val userAnswers = emptyUserAnswers
        .set(ReporterTypePage, ReporterType.Individual)
        .success
        .value
        .set(DoYouHaveNINPage, false)
        .success
        .value
        .set(NonUkNamePage, nonUkName)
        .success
        .value
        .set(IndividualContactEmailPage, TestEmail)
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation
        .convertToPrimary(userAnswers)
        .value mustBe ContactInformation(
        IndividualDetails(nonUkName.givenName, None, nonUkName.familyName),
        TestEmail,
        None,
        None
      )
    }

    "must return PrimaryContact for the input 'UserAnswers with ReporterType as Sole trader'" in {
      val userAnswers = emptyUserAnswers
        .set(DoYouHaveUniqueTaxPayerReferencePage, true)
        .success
        .value
        .set(ReporterTypePage, ReporterType.Sole)
        .success
        .value
        .set(SoleNamePage, name)
        .success
        .value
        .set(IndividualContactEmailPage, TestEmail)
        .success
        .value
        .set(IndividualHaveContactTelephonePage, false)
        .success
        .value

      ContactInformation
        .convertToPrimary(userAnswers)
        .value mustBe ContactInformation(IndividualDetails(name.firstName, None, name.lastName), TestEmail, None, None)
    }

    "must return SecondaryContact for the input 'Business with/without Id UserAnswers' " in {
      val userAnswers = emptyUserAnswers
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, TestEmail)
        .success
        .value
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactNamePage, name.fullName)
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
        .set(SndContactPhonePage, TestPhoneNumber)
        .success
        .value

      val expectedValue = ContactInformation(OrganisationDetails(name.fullName), TestEmail, Some(TestPhoneNumber), None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }

    "must return SecondaryContact for the input 'Business with/without Id UserAnswers' when SndConHavePhonePage is false" in {
      val userAnswers = emptyUserAnswers
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, TestEmail)
        .success
        .value
        .set(SndContactNamePage, name.fullName)
        .success
        .value
        .set(SndConHavePhonePage, false)
        .success
        .value

      val expectedValue = ContactInformation(OrganisationDetails(name.fullName), TestEmail, None, None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }

    "must return None when SecondContactPage is true and SndConHavePhonePage is true and SndContactPhonePage is empty" in {
      val userAnswers = emptyUserAnswers
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactEmailPage, TestEmail)
        .success
        .value
        .set(SecondContactPage, true)
        .success
        .value
        .set(SndContactNamePage, name.fullName)
        .success
        .value
        .set(SndConHavePhonePage, true)
        .success
        .value
        .set(SndContactPhonePage, TestPhoneNumber)
        .success
        .value

      val expectedValue = ContactInformation(OrganisationDetails(name.fullName), TestEmail, Some(TestPhoneNumber), None)
      ContactInformation.convertToSecondary(userAnswers).value mustBe Some(expectedValue)
    }
  }
}
