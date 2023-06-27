package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait ReporterType

object ReporterType extends Enumerable.Implicits {

  case object Option1 extends WithName("option1") with ReporterType
  case object Option2 extends WithName("option2") with ReporterType

  val values: Seq[ReporterType] = Seq(
    Option1,
    Option2
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"reporterType.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[ReporterType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
