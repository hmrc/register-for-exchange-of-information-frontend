package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ContactNameFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("contactName.error.required")
        .verifying(maxLength(1000, "contactName.error.length"))
    )
}
