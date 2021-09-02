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

  def contactPhone: Option[Row] = userAnswers.get(pages.ContactPhonePage) map {
    answer =>
      toRow(
        msgKey = "contactPhone",
        content = msg"site.edit",
        href = routes.ContactPhoneController.onPageLoad(CheckMode).url
      )
  }

  def secondContact: Option[Row] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      toRow(
        msgKey = "secondContact",
        content = msg"site.edit",
        href = routes.SecondContactController.onPageLoad(CheckMode).url
      )
  }

  def isContactTelephone: Option[Row] = userAnswers.get(pages.IsContactTelephonePage) map {
    answer =>
      toRow(
        msgKey = "isContactTelephone",
        content = msg"site.edit",
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url
      )
  }

  def contactName: Option[Row] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      toRow(
        msgKey = "contactName",
        content = msg"site.edit",
        href = routes.ContactNameController.onPageLoad(CheckMode).url
      )
  }

  def contactEmail: Option[Row] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        content = msg"site.edit",
        href = routes.ContactEmailController.onPageLoad(CheckMode).url
      )
  }

}
