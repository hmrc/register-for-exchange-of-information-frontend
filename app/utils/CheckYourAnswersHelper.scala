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
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(val userAnswers: UserAnswers, val maxVisibleChars: Int = 100)(implicit val messages: Messages) extends RowBuilder {

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

  def bussinessType: Option[Row] = userAnswers.get(pages.BusinessTypePage) map {
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
        value = msg"site.edit",
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode).url
      )
  }

  def whatAreYouRegisteringAs: Option[Row] = userAnswers.get(pages.WhatAreYouRegisteringAsPage) map {
    answer =>
      toRow(
        msgKey = "whatAreYouRegisteringAs",
        value = msg"site.edit",
        href = routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode).url
      )
  }

  def doYouHaveNIN: Option[Row] = userAnswers.get(pages.DoYouHaveNINPage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveNIN",
        value = msg"site.edit",
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
