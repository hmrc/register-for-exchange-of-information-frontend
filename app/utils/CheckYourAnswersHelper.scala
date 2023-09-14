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

package utils

import controllers.routes
import models.matching.OrgRegistrationInfo
import models.{CheckMode, UserAnswers}
import pages.{RegistrationInfoPage, SelectAddressPage, SndContactPhonePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class CheckYourAnswersHelper(val userAnswers: UserAnswers, val maxVisibleChars: Int = 100, countryListFactory: CountryListFactory)(implicit
  val messages: Messages
) extends RowBuilder {

  def confirmBusiness: Option[SummaryListRow] = {
    val paragraphClass = """govuk-!-margin-0"""
    (userAnswers.get(pages.IsThisYourBusinessPage), userAnswers.get(RegistrationInfoPage)) match {
      case (Some(true), Some(registrationInfo: OrgRegistrationInfo)) =>
        val businessName = registrationInfo.name
        val address      = registrationInfo.address
        for {
          countryName <- countryListFactory.getDescriptionFromCode(address.countryCode)
        } yield toRow(
          msgKey = "businessWithIDName",
          value = HtmlContent(s"""
                        |<p>$businessName</p>
                        |<p class=$paragraphClass>${address.addressLine1}</p>
                        |${address.addressLine2.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                        |${address.addressLine3.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                        |${address.addressLine4.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                        |<p class=$paragraphClass>${address.postCodeFormatter(address.postalCode).getOrElse("")}</p>
                        |${if (address.countryCode.toUpperCase != "GB") s"<p $paragraphClass>$countryName</p>" else ""}
                        |""".stripMargin),
          href = routes.ReporterTypeController.onPageLoad(CheckMode).url
        )
      case _ => None
    }
  }

  def whatIsTradingName: Option[SummaryListRow] =
    userAnswers.get(pages.BusinessWithoutIDNamePage) map {
      x =>
        val value = userAnswers.get(pages.WhatIsTradingNamePage) match {
          case Some(answer) => answer
          case None         => "None"
        }

        toRow(
          msgKey = "whatIsTradingName",
          value = Text(s"$value"),
          href = routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode).url
        )
    }

  def selectAddress: Option[SummaryListRow] = userAnswers.get(SelectAddressPage) map {
    answer =>
      toRow(
        msgKey = "selectAddress",
        value = HtmlContent(s"${answer.replace(",", "<br>")}"),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def businessWithoutIDName: Option[SummaryListRow] = userAnswers.get(pages.BusinessWithoutIDNamePage) map {
    answer =>
      toRow(
        msgKey = "businessWithoutIDName",
        value = Text(s"$answer"),
        href = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourPostcode: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourPostcodePage) map {
    _ =>
      toRow(
        msgKey = "whatIsYourPostcode",
        value = Text(messages("site.edit")),
        href = routes.WhatIsYourPostcodeController.onPageLoad(CheckMode).url
      )
  }

  def nonUkName: Option[SummaryListRow] = userAnswers.get(pages.NonUkNamePage) map {
    answer =>
      toRow(
        msgKey = "nonUkName",
        value = Text(s"${answer.givenName} ${answer.familyName}"),
        href = routes.NonUkNameController.onPageLoad(CheckMode).url
      )
  }

  def soleName: Option[SummaryListRow] = userAnswers.get(pages.SoleNamePage) map {
    _ =>
      toRow(
        msgKey = "soleName",
        value = Text(messages("site.edit")),
        href = routes.SoleNameController.onPageLoad(CheckMode).url
      )
  }

  def addressUK: Option[SummaryListRow] = userAnswers.get(pages.AddressUKPage) map {
    answer =>
      toRow(
        msgKey = "addressUK",
        value = formatAddress(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def individualAddressWithoutID: Option[SummaryListRow] = userAnswers.get(pages.IndividualAddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.individual",
        value = formatAddress(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def businessAddressWithoutID: Option[SummaryListRow] = userAnswers.get(pages.BusinessAddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.business",
        value = formatAddress(answer),
        href = routes.BusinessAddressWithoutIdController.onPageLoad(CheckMode).url
      )
  }

  def doYouLiveInTheUK(): Option[SummaryListRow] = userAnswers.get(pages.DoYouLiveInTheUKPage) map {
    answer =>
      toRow(
        msgKey = "doYouLiveInTheUK",
        value = yesOrNo(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def nonUkNameController: Option[SummaryListRow] = userAnswers.get(pages.NonUkNamePage) map {
    _ =>
      toRow(
        msgKey = "nonUkNameController",
        value = Text(messages("site.edit")),
        href = routes.NonUkNameController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourDateOfBirth: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourDateOfBirthPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourDateOfBirth",
        value = Text(s"${answer.format(dateFormatter)}"),
        href = routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode).url
      )
  }

  def dateOfBirthWithoutId: Option[SummaryListRow] = userAnswers.get(pages.DateOfBirthWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "dateOfBirthWithoutId",
        value = Text(s"${answer.format(dateFormatter)}"),
        href = routes.DateOfBirthWithoutIdController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourName: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourNamePage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourName",
        value = Text(s"${answer.firstName} ${answer.lastName}"),
        href = routes.WhatIsYourNameController.onPageLoad(CheckMode).url
      )
  }

  def nino: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourNationalInsuranceNumber",
        value = Text(s"$answer"),
        href = routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode).url
      )
  }

  def isThisYourBusiness: Option[SummaryListRow] = userAnswers.get(pages.IsThisYourBusinessPage) map {
    _ =>
      toRow(
        msgKey = "isThisYourBusiness",
        value = Text(messages("site.edit")),
        href = routes.IsThisYourBusinessController.onPageLoad(CheckMode).url
      )
  }

  def businessName: Option[SummaryListRow] = userAnswers.get(pages.BusinessNamePage) map {
    answer =>
      toRow(
        msgKey = "businessName",
        value = Text(messages("site.edit")),
        href = routes.BusinessNameController.onPageLoad(CheckMode).url
      )
  }

  def uTR: Option[SummaryListRow] = userAnswers.get(pages.UTRPage) map {
    _ =>
      toRow(
        msgKey = "uTR",
        value = Text(messages("site.edit")),
        href = routes.UTRController.onPageLoad(CheckMode).url
      )
  }

  def reporterType: Option[SummaryListRow] = userAnswers.get(pages.ReporterTypePage) map {
    value =>
      toRow(
        msgKey = "reporterType",
        value = Text(messages(s"reporterType.${value.toString}")),
        href = routes.ReporterTypeController.onPageLoad(CheckMode).url
      )
  }

  def registeredAddressInUk: Option[SummaryListRow] = userAnswers.get(pages.RegisteredAddressInUKPage) map {
    answer =>
      toRow(
        msgKey = "registeredAddressInUK",
        value = yesOrNo(answer),
        href = routes.RegisteredAddressInUKController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveUniqueTaxPayerReference: Option[SummaryListRow] = userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveUniqueTaxPayerReference",
        value = yesOrNo(answer),
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveNIN: Option[SummaryListRow] = userAnswers.get(pages.DoYouHaveNINPage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveNIN",
        value = yesOrNo(answer),
        href = routes.DoYouHaveNINController.onPageLoad(CheckMode).url
      )
  }

  def sndConHavePhone: Option[SummaryListRow] = userAnswers.get(pages.SndConHavePhonePage) map {
    answer =>
      toRow(
        msgKey = "sndConHavePhone",
        value = yesOrNo(answer),
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url
      )
  }

  def sndContactPhone: Option[SummaryListRow] = userAnswers.get(pages.SndConHavePhonePage) map {
    _ =>
      val value = userAnswers.get(SndContactPhonePage).getOrElse("None")
      toRow(
        msgKey = "sndContactPhone",
        value = Text(s"$value"),
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url
      )
  }

  def sndContactEmail: Option[SummaryListRow] = userAnswers.get(pages.SndContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "sndContactEmail",
        value = Text(s"$answer"),
        href = routes.SndContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def sndContactName: Option[SummaryListRow] = userAnswers.get(pages.SndContactNamePage) map {
    answer =>
      toRow(
        msgKey = "sndContactName",
        value = Text(s"$answer"),
        href = routes.SndContactNameController.onPageLoad(CheckMode).url
      )
  }

  def secondContact: Option[SummaryListRow] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      toRow(
        msgKey = "secondContact",
        value = yesOrNo(answer),
        href = routes.SecondContactController.onPageLoad(CheckMode).url
      )
  }

  def contactPhone: Option[SummaryListRow] = userAnswers.get(pages.ContactPhonePage) match {
    case Some(answer) => buildContactPhoneRow(answer)
    case None         => buildContactPhoneRow("None")
  }

  private def buildContactPhoneRow(value: String): Option[SummaryListRow] =
    Some(
      toRow(
        msgKey = "contactPhone",
        value = Text(s"$value"),
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url
      )
    )

  def isContactTelephone: Option[SummaryListRow] = userAnswers.get(pages.IsContactTelephonePage) map {
    answer =>
      toRow(
        msgKey = "isContactTelephone",
        value = yesOrNo(answer),
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url
      )
  }

  def contactName: Option[SummaryListRow] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      toRow(
        msgKey = "contactName",
        value = Text(s"$answer"),
        href = routes.ContactNameController.onPageLoad(CheckMode).url
      )
  }

  def contactEmail: Option[SummaryListRow] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        value = Text(s"$answer"),
        href = routes.ContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def individualContactEmail: Option[SummaryListRow] = userAnswers.get(pages.IndividualContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        value = Text(s"$answer"),
        href = routes.IndividualContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def individualContactPhone: Option[SummaryListRow] = {
    val numberOrNot = userAnswers.get(pages.IndividualContactPhonePage) match {
      case Some(answer) => answer
      case _            => "None"
    }
    Some(
      toRow(
        msgKey = "individualContactPhone",
        value = Text(s"$numberOrNot"),
        href = routes.IndividualHaveContactTelephoneController.onPageLoad(CheckMode).url
      )
    )
  }

}
