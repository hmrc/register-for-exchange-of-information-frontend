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

package viewmodels

import models.{Regime, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList
import utils.{CheckYourAnswersHelper, CountryListFactory}

object CheckYourAnswersViewModel {

  def buildPages(userAnswers: UserAnswers, regime: Regime, countryFactory: CountryListFactory, isBusiness: Boolean)(implicit
    messages: Messages
  ): Seq[Section] = {

    val helper            = new CheckYourAnswersHelper(userAnswers, regime, countryListFactory = countryFactory)
    val (contact, header) = if (isBusiness) ("firstContact", "businessDetails") else ("contactDetails", "individualDetails")

    val regDetails     = messages(s"checkYourAnswers.$header.h2")
    val contactHeading = messages(s"checkYourAnswers.$contact.h2")
    val secContact     = if (isBusiness) Seq(Section(messages("checkYourAnswers.secondContact.h2"), buildSecondContact(helper))) else Nil

    Seq(
      Section(regDetails, buildDetails(userAnswers, helper)),
      Section(contactHeading, buildFirstContact(helper))
    ) ++: secContact
  }

  private def buildDetails(userAnswers: UserAnswers, helper: CheckYourAnswersHelper): Seq[SummaryList.Row] =
    if (userAnswers.get(pages.BusinessTypePage).isDefined) {
      Seq(
        helper.confirmBusiness
      ).flatten
    } else {
      Seq(
        helper.doYouHaveUniqueTaxPayerReference,
        helper.confirmBusiness,
        helper.whatAreYouRegisteringAs,
        helper.doYouHaveNIN,
        helper.nino,
        helper.whatIsYourName,
        helper.nonUkName,
        helper.whatIsYourDateOfBirth,
        helper.businessWithoutIDName,
        helper.whatIsTradingName,
        helper.addressWithoutIdBusiness,
        helper.addressUK,
        helper.selectAddress
      ).flatten
    }

  private def buildSecondContact(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] =
    Seq(
      helper.secondContact,
      helper.sndContactName,
      helper.sndContactEmail,
      helper.sndContactPhone
    ).flatten

  private def buildFirstContact(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] =
    Seq(
      helper.contactName,
      helper.contactEmail,
      helper.contactPhone
    ).flatten
}