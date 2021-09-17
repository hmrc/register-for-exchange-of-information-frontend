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

  def sndConHavePhone: Option[Row] = userAnswers.get(pages.SndConHavePhonePage) map {
    answer =>
      Row(
        key = Key(msg"sndConHavePhone.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.SndConHavePhoneController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"sndConHavePhone.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-second-contact-have-phone")
          )
        )
      )
  }

  def sndContactPhone: Option[Row] = userAnswers.get(pages.SndContactPhonePage) map {
    answer =>
      Row(
        key = Key(msg"sndContactPhone.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.SndContactPhoneController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"sndContactPhone.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-second-contact-phone")
          )
        )
      )
  }

  def sndContactEmail: Option[Row] = userAnswers.get(pages.SndContactEmailPage) map {
    answer =>
      Row(
        key = Key(msg"sndContactEmail.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.SndContactEmailController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"sndContactEmail.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-second-contact-email")
          )
        )
      )
  }

  def sndContactName: Option[Row] = userAnswers.get(pages.SndContactNamePage) map {
    answer =>
      Row(
        key = Key(msg"sndContactName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.SndContactNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"sndContactName.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-second-contact-name")
          )
        )
      )
  }

  def secondContact: Option[Row] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      Row(
        key = Key(msg"secondContact.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.SecondContactController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"secondContact.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-second-contact")
          )
        )
      )
  }

  def contactPhone: Option[Row] = userAnswers.get(pages.ContactPhonePage) map {
    answer =>
      Row(
        key = Key(msg"contactPhone.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.ContactPhoneController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"contactPhone.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-contact-phone")
          )
        )
      )
  }

  def isContactTelephone: Option[Row] = userAnswers.get(pages.IsContactTelephonePage) map {
    answer =>
      Row(
        key = Key(msg"isContactTelephone.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.IsContactTelephoneController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"isContactTelephone.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-is-contact-telephone")
          )
        )
      )
  }

  def contactName: Option[Row] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      Row(
        key = Key(msg"contactName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.ContactNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"contactName.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-contact-name")
          )
        )
      )
  }

  def contactEmail: Option[Row] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      Row(
        key = Key(msg"contactEmail.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.ContactEmailController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"contactEmail.checkYourAnswersLabel")),
            attributes = Map("id" -> "change-contact-email")
          )
        )
      )
  }

  private def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }
}
