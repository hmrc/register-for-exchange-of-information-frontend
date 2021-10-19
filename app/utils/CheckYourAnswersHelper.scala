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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.{AddressUKPage, BusinessNamePage, MatchingInfoPage, SelectAddressPage}
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._
import utils.CountryListFactory

class CheckYourAnswersHelper(val userAnswers: UserAnswers, val maxVisibleChars: Int = 100, countryListFactory: CountryListFactory)(implicit
  val messages: Messages
) extends RowBuilder {

  def whatIsTradingName: Option[Row] = userAnswers.get(pages.WhatIsTradingNamePage) map {
    answer =>
      toRow(
        msgKey = "whatIsTradingName",
        value = msg"site.edit",
        href = routes.WhatIsTradingNameController.onPageLoad(CheckMode).url
      )
  }

  def businessHaveDifferentName: Option[Row] = userAnswers.get(pages.BusinessHaveDifferentNamePage) map {
    answer =>
      toRow(
        msgKey = "businessHaveDifferentName",
        value = msg"site.edit",
        href = routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode).url
      )
  }

  def buildDetails(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple4(
      helper.businessType,
      helper.nino,
      helper.businessWithoutIDName,
      helper.nonUkName
    )

    pagesToCheck match {
      case (Some(_), None, None, None) =>
        //Business with ID (inc. Sole proprietor)
        Seq(
          helper.confirmBusiness
        ).flatten

      case (None, Some(_), None, None) =>
        //Individual with ID
        Seq(
          helper.doYouHaveUniqueTaxPayerReference,
          helper.whatAreYouRegisteringAs,
          helper.doYouHaveNIN,
          helper.nino,
          helper.whatIsYourName,
          helper.whatIsYourDateOfBirth
        ).flatten
      case (None, None, Some(_), None) =>
        //Business without ID
        Seq(
          helper.doYouHaveUniqueTaxPayerReference,
          helper.whatAreYouRegisteringAs,
          helper.businessWithoutIDName,
          helper.addressWithoutIdBusiness
        ).flatten
      case (None, None, None, Some(_)) =>
        //Individual without ID
        Seq(
          helper.doYouHaveUniqueTaxPayerReference,
          helper.whatAreYouRegisteringAs,
          helper.doYouHaveNIN,
          helper.nonUkName,
          helper.whatIsYourDateOfBirth,
          helper.doYouLiveInTheUK,
          helper.addressWithoutIdIndividual,
          helper.addressUK,
          helper.selectAddress //ToDo hook selectAddress logic
        ).flatten
      case _ =>
        //All pages
        Seq(
          helper.doYouHaveUniqueTaxPayerReference,
          helper.confirmBusiness,
          helper.nino,
          helper.whatIsYourName,
          helper.whatIsYourDateOfBirth,
          helper.whatAreYouRegisteringAs,
          helper.businessWithoutIDName,
          helper.addressUK,
          helper.doYouHaveNIN,
          helper.nonUkName,
          helper.doYouLiveInTheUK,
          helper.addressUK
        ).flatten
    }
  }

  def confirmBusiness: Option[Row] =
    userAnswers.get(pages.IsThisYourBusinessPage) match {
      case Some(true) =>
        for {
          matchingInfo <- userAnswers.get(MatchingInfoPage)
          businessName <- matchingInfo.name
          address      <- matchingInfo.address
          countryName  <- countryListFactory.getDescriptionFromCode(address.countryCode)
        } yield toRow(
          msgKey = "businessWithoutIDName",
          value = Html(s"""
                  $businessName<br><br>
                  ${address.addressLine1}<br>
                  ${address.addressLine2.fold("")(
            address => s"$address<br>"
          )}<br>
                  ${address.addressLine3}
                  ${address.addressLine4.fold("")(
            address => s"$address<br>"
          )}
                  ${address.postalCode.fold("")(
            postcode => s"$postcode<br>"
          )}
                  $countryName
                  """),
          href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode).url
        )
      case _ => None
    }

  def selectAddress: Option[Row] = userAnswers.get(SelectAddressPage) map {
    answer =>
      toRow(
        msgKey = "selectAddress",
        value = Html(s"${answer.replace(",", "<br>")}"),
        href = routes.SelectAddressController.onPageLoad(CheckMode).url
      )
  }

  def businessWithoutIDName: Option[Row] = userAnswers.get(pages.BusinessWithoutIDNamePage) map {
    answer =>
      toRow(
        msgKey = "businessWithoutIDName",
        value = lit"$answer",
        href = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourPostcode: Option[Row] = userAnswers.get(pages.WhatIsYourPostcodePage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourPostcode",
        value = msg"site.edit",
        href = routes.WhatIsYourPostcodeController.onPageLoad(CheckMode).url
      )
  }

  def nonUkName: Option[Row] = userAnswers.get(pages.NonUkNamePage) map {
    answer =>
      toRow(
        msgKey = "nonUkName",
        value = lit"${answer.givenName} ${answer.familyName}",
        href = routes.NonUkNameController.onPageLoad(CheckMode).url
      )
  }

  def soleDateOfBirth: Option[Row] = userAnswers.get(pages.SoleDateOfBirthPage) map {
    answer =>
      toRow(
        msgKey = "soleDateOfBirth",
        value = msg"site.edit",
        href = routes.SoleDateOfBirthController.onPageLoad(CheckMode).url
      )
  }

  def soleName: Option[Row] = userAnswers.get(pages.SoleNamePage) map {
    answer =>
      toRow(
        msgKey = "soleName",
        value = msg"site.edit",
        href = routes.SoleNameController.onPageLoad(CheckMode).url
      )
  }

  def addressUK: Option[Row] = userAnswers.get(pages.AddressUKPage) map {
    answer =>
      toRow(
        msgKey = "addressUK",
        value = formatAddress(answer),
        href = routes.AddressUKController.onPageLoad(CheckMode).url
      )
  }

  def addressWithoutIdIndividual: Option[Row] = userAnswers.get(pages.AddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.individual",
        value = formatAddress(answer),
        href = routes.AddressWithoutIdController.onPageLoad(CheckMode).url
      )
  }

  def addressWithoutIdBusiness: Option[Row] = userAnswers.get(pages.AddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.business",
        value = formatAddress(answer),
        href = routes.AddressWithoutIdController.onPageLoad(CheckMode).url
      )
  }


  def doYouLiveInTheUK: Option[Row] = userAnswers.get(pages.DoYouLiveInTheUKPage) map {
    answer =>
      toRow(
        msgKey = "doYouLiveInTheUK",
        value = yesOrNo(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url
      )
  }

  def nonUkNameController: Option[Row] = userAnswers.get(pages.NonUkNamePage) map {
    answer =>
      toRow(
        msgKey = "nonUkNameController",
        value = msg"site.edit",
        href = routes.NonUkNameController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourDateOfBirth: Option[Row] = userAnswers.get(pages.WhatIsYourDateOfBirthPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourDateOfBirth",
        value = lit"${answer.format(dateFormatter)}",
        href = routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode).url
      )
  }

  def whatIsYourName: Option[Row] = userAnswers.get(pages.WhatIsYourNamePage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourName",
        value = lit"${answer.firstName} ${answer.lastName}",
        href = routes.WhatIsYourNameController.onPageLoad(CheckMode).url
      )
  }

  def nino: Option[Row] = userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourNationalInsuranceNumber",
        value = lit"$answer",
        href = routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode).url
      )
  }

  def isThisYourBusiness: Option[Row] = userAnswers.get(pages.IsThisYourBusinessPage) map {
    answer =>
      toRow(
        href = routes.IsThisYourBusinessController.onPageLoad(CheckMode).url,
        msgKey = "isThisYourBusiness",
        value = msg"site.edit"
      )
  }

  def businessName: Option[Row] = userAnswers.get(pages.BusinessNamePage) map {
    answer =>
      toRow(
        msgKey = "businessName",
        value = msg"site.edit",
        href = routes.BusinessNameController.onPageLoad(CheckMode).url
      )
  }

  def uTR: Option[Row] = userAnswers.get(pages.UTRPage) map {
    answer =>
      toRow(
        msgKey = "uTR",
        value = msg"site.edit",
        href = routes.UTRController.onPageLoad(CheckMode).url
      )
  }

  def businessType: Option[Row] = userAnswers.get(pages.BusinessTypePage) map {
    answer =>
      toRow(
        msgKey = "bussinessType",
        value = msg"site.edit",
        href = routes.BusinessTypeController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveUniqueTaxPayerReference: Option[Row] = userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveUniqueTaxPayerReference",
        value = yesOrNo(answer),
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode).url
      )
  }

  def whatAreYouRegisteringAs: Option[Row] = userAnswers.get(pages.WhatAreYouRegisteringAsPage) map {
    answer =>
      toRow(
        msgKey = "whatAreYouRegisteringAs",
        value = msg"whatAreYouRegisteringAs.$answer",
        href = routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveNIN: Option[Row] = userAnswers.get(pages.DoYouHaveNINPage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveNIN",
        value = yesOrNo(answer),
        href = routes.DoYouHaveNINController.onPageLoad(CheckMode).url
      )
  }

  /** *************
    *    Second contact
    * *************
    */

  def buildSecondContact: Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple4(
      secondContact,
      sndContactName,
      sndContactEmail,
      sndContactPhone
    )

    pagesToCheck match {
      case (Some(_), None, None, None) =>
        //No second contact
        Seq(
          secondContact
        ).flatten
      case (Some(_), Some(_), Some(_), None) =>
        //No second contact phone
        Seq(
          secondContact,
          sndContactName,
          sndContactEmail
        ).flatten
      case _ =>
        //All pages
        Seq(
          secondContact,
          sndContactName,
          sndContactEmail,
          sndContactPhone
        ).flatten
    }
  }

  def sndConHavePhone: Option[Row] = userAnswers.get(pages.SndConHavePhonePage) map {
    answer =>
      toRow(
        msgKey = "sndConHavePhone",
        value = yesOrNo(answer),
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url
      )
  }

  def sndContactPhone: Option[Row] = userAnswers.get(pages.SndContactPhonePage) match {
    case Some(answer)                                                      => buildSndContactPhoneRow(answer)
    case None if userAnswers.get(pages.SecondContactPage).getOrElse(false) => buildSndContactPhoneRow("None")
    case _                                                                 => None
  }

  private def buildSndContactPhoneRow(value: String): Option[Row] =
    Some(
      toRow(
        msgKey = "sndContactPhone",
        value = lit"$value",
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url
      )
    )

  def sndContactEmail: Option[Row] = userAnswers.get(pages.SndContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "sndContactEmail",
        value = lit"$answer",
        href = routes.SndContactEmailController.onPageLoad(CheckMode).url
      )
  }

  def sndContactName: Option[Row] = userAnswers.get(pages.SndContactNamePage) map {
    answer =>
      toRow(
        msgKey = "sndContactName",
        value = lit"$answer",
        href = routes.SndContactNameController.onPageLoad(CheckMode).url
      )
  }

  def secondContact: Option[Row] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      toRow(
        msgKey = "secondContact",
        value = yesOrNo(answer),
        href = routes.SecondContactController.onPageLoad(CheckMode).url
      )
  }

  /** *************
    *    First contact
    * *************
    */

  def buildFirstContact: Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple3(
      contactName,
      contactEmail,
      contactPhone
    )

    pagesToCheck match {
      case (Some(_), Some(_), None) =>
        //No contact telephone
        Seq(
          contactName,
          contactEmail
        ).flatten
      case _ =>
        //All pages
        Seq(
          contactName,
          contactEmail,
          contactPhone
        ).flatten
    }
  }

  def contactPhone: Option[Row] = userAnswers.get(pages.ContactPhonePage) match {
    case Some(answer) => buildContactPhoneRow(answer)
    case None         => buildContactPhoneRow("None")
  }

  private def buildContactPhoneRow(value: String): Option[Row] =
    Some(
      toRow(
        msgKey = "contactPhone",
        value = lit"$value",
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url
      )
    )

  def isContactTelephone: Option[Row] = userAnswers.get(pages.IsContactTelephonePage) map {
    answer =>
      toRow(
        msgKey = "isContactTelephone",
        value = yesOrNo(answer),
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url
      )
  }

  def contactName: Option[Row] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      toRow(
        msgKey = "contactName",
        value = lit"$answer",
        href = routes.ContactNameController.onPageLoad(CheckMode).url
      )
  }

  def contactEmail: Option[Row] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        value = lit"$answer",
        href = routes.ContactEmailController.onPageLoad(CheckMode).url
      )
  }
}
