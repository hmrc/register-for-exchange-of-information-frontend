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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.{Content, MessageInterpolators, Text}

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

  private[utils] def toRow(msgKey: String, content: Content, href: String, columnWidth: String = "govuk-!-width-one-half")(implicit messages: Messages): Row = {
    val message         = MessageInterpolators(StringContext.apply(s"$msgKey.checkYourAnswersLabel")).msg()
    val camelCaseGroups = "(\\b[a-z]+|\\G(?!^))((?:[A-Z]|\\d+)[a-z]*)"
    Row(
      key = Key(message, classes = Seq(columnWidth)),
      value = Value(content),
      actions = List(
        Action(
          content = msg"site.edit",
          href = href,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(message)),
          attributes = Map("id" -> msgKey.replaceAll(camelCaseGroups, "$1-$2").toLowerCase)
        )
      )
    )
  }

  private[utils] def formatMaxChars(text: String, maxVisibleChars: Int = 100) = {
    val label = if (maxVisibleChars > 0 && text.length > maxVisibleChars) text.take(maxVisibleChars) + "..." else text
    lit"$label"
  }

}
