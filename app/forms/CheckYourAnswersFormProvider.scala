package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class CheckYourAnswersFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("checkYourAnswers.error.required")
        .verifying(maxLength(100, "checkYourAnswers.error.length"))
    )
}
