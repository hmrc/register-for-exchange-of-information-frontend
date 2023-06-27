package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ReporterTypePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReporterTypeSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReporterTypePage).map {
      answers =>

        val value = ValueViewModel(
          HtmlContent(
            answers.map {
              answer => HtmlFormat.escape(messages(s"reporterType.$answer")).toString
            }
            .mkString(",<br>")
          )
        )

        SummaryListRowViewModel(
          key     = "reporterType.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReporterTypeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("reporterType.change.hidden"))
          )
        )
    }
}
