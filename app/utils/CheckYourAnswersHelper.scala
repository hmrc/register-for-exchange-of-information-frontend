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
import models.Address.GBCountryCode
import models.matching.OrgRegistrationInfo
import models.{CheckMode, UserAnswers}
import pages._
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
                        |${if (address.countryCode.toUpperCase != GBCountryCode) s"<p $paragraphClass>$countryName</p>" else ""}
                        |""".stripMargin),
          href = if (userAnswers.get(AutoMatchedUTRPage).isEmpty) {
            routes.ReporterTypeController.onPageLoad(CheckMode).url
          } else {
            routes.UnableToChangeBusinessController.onPageLoad().url
          }
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
          msgKey = WhatIsTradingNamePage.toString,
          value = Text(value),
          href = routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode).url
        )
    }

  def selectAddress: Option[SummaryListRow] = userAnswers.get(SelectAddressPage) map {
    answer =>
      toRow(
        msgKey = SelectAddressPage.toString,
        value = HtmlContent(s"${answer.replace(",", "<br>")}"),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def businessWithoutIDName: Option[SummaryListRow] = userAnswers.get(pages.BusinessWithoutIDNamePage) map {
    answer =>
      toRow(
        msgKey = BusinessWithoutIDNamePage.toString,
        value = Text(answer),
        href = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode).url
      )
  }

  def nonUkName: Option[SummaryListRow] = userAnswers.get(pages.NonUkNamePage) map {
    answer =>
      toRow(
        msgKey = NonUkNamePage.toString,
        value = Text(s"${answer.givenName} ${answer.familyName}"),
        href = routes.NonUkNameController.onPageLoad(CheckMode).url
      )
  }

  def addressUK: Option[SummaryListRow] = userAnswers.get(pages.AddressUKPage) map {
    answer =>
      toRow(
        msgKey = AddressUKPage.toString,
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
        msgKey = DoYouLiveInTheUKPage.toString,
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
        msgKey = WhatIsYourDateOfBirthPage.toString,
        value = Text(s"${answer.format(dateFormatter)}"),
        href = routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode).url
      )
  }

  def dateOfBirthWithoutId: Option[SummaryListRow] = userAnswers.get(pages.DateOfBirthWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = DateOfBirthWithoutIdPage.toString,
        value = Text(s"${answer.format(dateFormatter)}"),
        href = routes.DateOfBirthWithoutIdController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourName: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourNamePage) map {
    answer =>
      toRow(
        msgKey = WhatIsYourNamePage.toString,
        value = Text(s"${answer.firstName} ${answer.lastName}"),
        href = routes.WhatIsYourNameController.onPageLoad(CheckMode).url
      )
  }

  def nino: Option[SummaryListRow] = userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage) map {
    answer =>
      toRow(
        msgKey = WhatIsYourNationalInsuranceNumberPage.toString,
        value = Text(answer.toString()),
        href = routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode).url
      )
  }

  def isThisYourBusiness: Option[SummaryListRow] = userAnswers.get(pages.IsThisYourBusinessPage) map {
    _ =>
      toRow(
        msgKey = IsThisYourBusinessPage.toString,
        value = Text(messages("site.edit")),
        href = routes.IsThisYourBusinessController.onPageLoad(CheckMode).url
      )
  }

  def businessName: Option[SummaryListRow] = userAnswers.get(pages.BusinessNamePage) map {
    _ =>
      toRow(
        msgKey = BusinessNamePage.toString,
        value = Text(messages("site.edit")),
        href = routes.BusinessNameController.onPageLoad(CheckMode).url
      )
  }

  def reporterType: Option[SummaryListRow] = userAnswers.get(pages.ReporterTypePage) map {
    value =>
      toRow(
        msgKey = ReporterTypePage.toString,
        value = Text(messages(s"reporterType.${value.toString}")),
        href = routes.ReporterTypeController.onPageLoad(CheckMode).url
      )
  }

  def registeredAddressInUk: Option[SummaryListRow] = userAnswers.get(pages.RegisteredAddressInUKPage) map {
    answer =>
      toRow(
        msgKey = RegisteredAddressInUKPage.toString,
        value = yesOrNo(answer),
        href = routes.RegisteredAddressInUKController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveUniqueTaxPayerReference: Option[SummaryListRow] = userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage) map {
    answer =>
      toRow(
        msgKey = DoYouHaveUniqueTaxPayerReferencePage.toString,
        value = yesOrNo(answer),
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveNIN: Option[SummaryListRow] = userAnswers.get(pages.DoYouHaveNINPage) map {
    answer =>
      toRow(
        msgKey = DoYouHaveNINPage.toString,
        value = yesOrNo(answer),
        href = routes.DoYouHaveNINController.onPageLoad(CheckMode).url
      )
  }

  def sndContactPhone: Option[SummaryListRow] = userAnswers.get(pages.SndConHavePhonePage) map {
    _ =>
      val value = userAnswers.get(SndContactPhonePage).getOrElse("None")
      toRow(
        msgKey = SndContactPhonePage.toString,
        value = Text(value),
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url
      )
  }

  def sndContactEmail: Option[SummaryListRow] = userAnswers.get(pages.SndContactEmailPage) map {
    answer =>
      toRow(
        msgKey = SndContactEmailPage.toString,
        value = Text(answer),
        href = routes.SndContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def sndContactName: Option[SummaryListRow] = userAnswers.get(pages.SndContactNamePage) map {
    answer =>
      toRow(
        msgKey = SndContactNamePage.toString,
        value = Text(answer),
        href = routes.SndContactNameController.onPageLoad(CheckMode).url
      )
  }

  def secondContact: Option[SummaryListRow] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      toRow(
        msgKey = SecondContactPage.toString,
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
        msgKey = ContactPhonePage.toString,
        value = Text(value),
        href = routes.ContactHavePhoneController.onPageLoad(CheckMode).url
      )
    )

  def contactName: Option[SummaryListRow] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      toRow(
        msgKey = ContactNamePage.toString,
        value = Text(answer),
        href = routes.ContactNameController.onPageLoad(CheckMode).url
      )
  }

  def contactEmail: Option[SummaryListRow] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      toRow(
        msgKey = ContactEmailPage.toString,
        value = Text(answer),
        href = routes.ContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def individualContactEmail: Option[SummaryListRow] = userAnswers.get(pages.IndividualContactEmailPage) map {
    answer =>
      toRow(
        msgKey = ContactEmailPage.toString,
        value = Text(answer),
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
        msgKey = IndividualContactPhonePage.toString,
        value = Text(numberOrNot),
        href = routes.IndividualHaveContactTelephoneController.onPageLoad(CheckMode).url
      )
    )
  }

}
