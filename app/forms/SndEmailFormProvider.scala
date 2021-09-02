package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class SndEmailFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("sndEmail.error.required")
        .verifying(maxLength(400, "sndEmail.error.length"))
    )
}
