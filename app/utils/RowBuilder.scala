/*
 * Copyright 2022 HM Revenue & Customs
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

import models.{Address, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.{Content, Html, MessageInterpolators, Text}

import java.time.format.DateTimeFormatter

trait RowBuilder {

  implicit val messages: Messages
  val userAnswers: UserAnswers
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private[utils] def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }

  private[utils] def messageWithPluralFormatter(msgKey: String*)(isPlural: Boolean, argIfPlural: String = "s", argIfSingular: String = ""): Text.Message =
    MessageInterpolators(StringContext.apply(msgKey.head))
      .msg()
      .withArgs(((if (isPlural) argIfPlural else argIfSingular) +: msgKey.tail): _*)

  private[utils] def toRow(msgKey: String, value: Content, href: String, columnWidth: String = "govuk-!-width-one-half")(implicit
    messages: Messages
  ): Row = {
    val message = MessageInterpolators(StringContext.apply(s"$msgKey.checkYourAnswersLabel")).msg()
    val hiddenText = if (messages.isDefinedAt(s"$msgKey.checkYourAnswersLabel.hiddenText")) {
      MessageInterpolators(StringContext.apply(s"$msgKey.checkYourAnswersLabel.hiddenText")).msg()
    } else {
      message
    }
    val camelCaseGroups = "(\\b[a-z]+|\\G(?!^))((?:[A-Z]|\\d+)[a-z]*)"
    Row(
      key = Key(message, classes = Seq(columnWidth)),
      value = Value(value),
      actions = List(
        Action(
          content = msg"site.edit",
          href = href,
          visuallyHiddenText = Some(hiddenText),
          attributes = Map("id" -> msgKey.replaceAll(camelCaseGroups, "$1-$2").toLowerCase)
        )
      )
    )
  }

  private[utils] def formatMaxChars(text: String, maxVisibleChars: Int = 100) = {
    val label = if (maxVisibleChars > 0 && text.length > maxVisibleChars) text.take(maxVisibleChars) + "..." else text
    lit"$label"
  }

  private[utils] def formatAddress(answer: Address): Html =
    Html(s"""
        ${answer.addressLine1}<br>
        ${answer.addressLine2.fold("")(
      address => s"$address<br>"
    )}
        ${answer.addressLine3}<br>
        ${answer.addressLine4.fold("")(
      address => s"$address<br>"
    )}
        ${answer.postCode.fold("")(
      postcode => s"$postcode<br>"
    )}
        ${answer.country.description}
     """)

}
