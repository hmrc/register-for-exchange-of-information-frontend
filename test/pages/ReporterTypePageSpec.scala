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

import models.ReporterType
import pages.behaviours.PageBehaviours
import models.ReporterType.{LimitedCompany, Sole}
import models.matching.{OrgRegistrationInfo, SafeId}
import models.register.response.details.AddressResponse
import models.{ReporterType, UserAnswers}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

class ReporterTypePageSpec extends PageBehaviours {
  implicit val reporterTypeArbitrary: Arbitrary[ReporterType] = Arbitrary(Gen.oneOf(ReporterType.values))

  "ReporterTypePage" - {

    beRetrievable[ReporterType](ReporterTypePage)

    beSettable[ReporterType](ReporterTypePage)

    beRemovable[ReporterType](ReporterTypePage)
  }

  "cleanup" - {

    "must remove organisation contact details when user selects a sole trader as business type" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(ContactNamePage, "SomeContact")
            .success
            .value
            .set(ContactEmailPage, "contact@email.com")
            .success
            .value
            .set(IsContactTelephonePage, true)
            .success
            .value
            .set(ContactPhonePage, "07540000000")
            .success
            .value
            .set(SecondContactPage, true)
            .success
            .value
            .set(SndContactNamePage, "SomeSecondContact")
            .success
            .value
            .set(SndContactEmailPage, "secondcontact@email.com")
            .success
            .value
            .set(SndConHavePhonePage, true)
            .success
            .value
            .set(SndContactPhonePage, "07540000000")
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(SafeId("safeId"), "Organisation", AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(BusinessWithoutIDNamePage, "Organisation")
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, "TradingName")
            .success
            .value
            .set(ReporterTypePage, Sole)
            .success
            .value

          result.get(IsThisYourBusinessPage) must not be defined
          result.get(ContactNamePage) must not be defined
          result.get(ContactEmailPage) must not be defined
          result.get(IsContactTelephonePage) must not be defined
          result.get(ContactPhonePage) must not be defined
          result.get(SecondContactPage) must not be defined
          result.get(SndContactNamePage) must not be defined
          result.get(SndContactEmailPage) must not be defined
          result.get(SndConHavePhonePage) must not be defined
          result.get(SndContactPhonePage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
      }
    }

    "must remove individual contact details when user selects any other organisation business type" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val result = userAnswers
            .set(IsThisYourBusinessPage, true)
            .success
            .value
            .set(RegistrationInfoPage, OrgRegistrationInfo(SafeId("safeId"), "Organisation", AddressResponse("Address", None, None, None, None, "GB")))
            .success
            .value
            .set(IndividualContactEmailPage, "contact@email.com")
            .success
            .value
            .set(IndividualHaveContactTelephonePage, true)
            .success
            .value
            .set(IndividualContactPhonePage, "07540000000")
            .success
            .value
            .set(BusinessWithoutIDNamePage, "Organisation")
            .success
            .value
            .set(BusinessHaveDifferentNamePage, true)
            .success
            .value
            .set(WhatIsTradingNamePage, "TradingName")
            .success
            .value
            .set(ReporterTypePage, LimitedCompany)
            .success
            .value

          result.get(IsThisYourBusinessPage) must not be defined
          result.get(RegistrationInfoPage) must not be defined
          result.get(IndividualContactEmailPage) must not be defined
          result.get(IndividualHaveContactTelephonePage) must not be defined
          result.get(IndividualContactPhonePage) must not be defined
          result.get(BusinessWithoutIDNamePage) must not be defined
          result.get(BusinessHaveDifferentNamePage) must not be defined
          result.get(WhatIsTradingNamePage) must not be defined
      }
    }
  }
}