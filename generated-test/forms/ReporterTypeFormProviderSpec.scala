package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.ReporterType
import play.api.data.FormError

class ReporterTypeFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new ReporterTypeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "reporterType.error.required"

    behave like checkboxField[ReporterType](
      form,
      fieldName,
      validValues  = ReporterType.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
