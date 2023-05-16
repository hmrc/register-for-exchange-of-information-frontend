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

import models.{Address, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import viewmodels.govuk.summarylist._

import java.time.format.DateTimeFormatter

trait RowBuilder {

  implicit val messages: Messages
  val userAnswers: UserAnswers
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private[utils] def yesOrNo(answer: Boolean): Content =
    if (answer) {
      Text(messages("site.yes"))
    } else {
      Text(messages("site.no"))
    }

  private[utils] def toRow(msgKey: String, value: Content, href: String, columnWidth: String = "govuk-!-width-one-half")(implicit
    messages: Messages
  ): SummaryListRow = {
    val message = messages(s"$msgKey.checkYourAnswersLabel")
    val hiddenText = if (messages.isDefinedAt(s"$msgKey.checkYourAnswersLabel.hiddenText")) {
      messages(s"$msgKey.checkYourAnswersLabel.hiddenText")
    } else {
      message
    }
    val camelCaseGroups = "(\\b[a-z]+|\\G(?!^))((?:[A-Z]|\\d+)[a-z]*)"
    SummaryListRowViewModel(
      key = Key(Text(message), classes = columnWidth),
      value = Value(value),
      actions = Seq(
        ActionItemViewModel(
          content = HtmlContent(
            s"""
            |<span aria-hidden="true">${messages("site.edit")}</span>
            |<span class="govuk-visually-hidden">${messages(hiddenText)}</span>
            |""".stripMargin
          ),
          href = href
        )
          .withAttribute("id" -> msgKey.replaceAll(camelCaseGroups, "$1-$2").toLowerCase)
      )
    )
  }

  private[utils] def formatAddress(answer: Address): HtmlContent =
    HtmlContent(s"""
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
