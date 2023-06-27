package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.set
import models.ReporterType

class ReporterTypeFormProvider @Inject() extends Mappings {

  def apply(): Form[Set[ReporterType]] =
    Form(
      "value" -> set(enumerable[ReporterType]("reporterType.error.required")).verifying(nonEmptySet("reporterType.error.required"))
    )
}
