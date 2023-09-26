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

import models.ReporterType.{Individual, Sole}
import models.{ReporterType, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object ReporterTypePage extends QuestionPage[ReporterType] {

  private val soleTraderCleanup = List(
    IsThisYourBusinessPage,
    RegistrationInfoPage,
    ContactNamePage,
    ContactEmailPage,
    ContactHavePhonePage,
    ContactPhonePage,
    SecondContactPage,
    SndContactNamePage,
    SndContactEmailPage,
    SndConHavePhonePage,
    SndContactPhonePage,
    BusinessWithoutIDNamePage,
    BusinessHaveDifferentNamePage,
    WhatIsTradingNamePage,
    BusinessNamePage,
    BusinessAddressWithoutIdPage
  )

  private val individualCleanup = List(
    IsThisYourBusinessPage,
    RegistrationInfoPage,
    RegisteredAddressInUKPage,
    DoYouHaveUniqueTaxPayerReferencePage,
    UTRPage,
    BusinessNamePage,
    SoleNamePage,
    ContactNamePage,
    ContactEmailPage,
    ContactHavePhonePage,
    ContactPhonePage,
    SecondContactPage,
    SndContactNamePage,
    SndContactEmailPage,
    SndConHavePhonePage,
    SndContactPhonePage,
    BusinessWithoutIDNamePage,
    BusinessHaveDifferentNamePage,
    WhatIsTradingNamePage,
    BusinessAddressWithoutIdPage
  )

  private val otherBusinessTyeCleanup = List(
    IsThisYourBusinessPage,
    RegistrationInfoPage,
    DoYouHaveNINPage,
    WhatIsYourNationalInsuranceNumberPage,
    WhatIsYourNamePage,
    WhatIsYourDateOfBirthPage,
    IndividualContactEmailPage,
    IndividualHaveContactTelephonePage,
    IndividualContactPhonePage,
    NonUkNamePage,
    DateOfBirthWithoutIdPage,
    DoYouLiveInTheUKPage,
    WhatIsYourPostcodePage,
    IndividualAddressWithoutIdPage,
    AddressUKPage,
    AddressLookupPage,
    SelectAddressPage,
    SelectedAddressLookupPage,
    SoleNamePage
  )

  override def path: JsPath = JsPath \ toString

  override def toString: String = "reporterType"

  override def cleanup(value: Option[ReporterType], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(Sole)       => soleTraderCleanup.foldLeft(Try(userAnswers))(PageLists.removePage)
    case Some(Individual) => individualCleanup.foldLeft(Try(userAnswers))(PageLists.removePage)
    case Some(_)          => otherBusinessTyeCleanup.foldLeft(Try(userAnswers))(PageLists.removePage)
    case _                => super.cleanup(value, userAnswers)
  }
}
