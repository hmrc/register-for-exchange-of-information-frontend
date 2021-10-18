package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class WhatIsTradingNameFormProviderSpec extends StringFieldBehaviours {

  val form = new WhatIsTradingNameFormProvider()()

  ".businessName" - {

    val fieldName   = "businessName"
    val requiredKey = "whatIsTradingName.error.businessName.required"
    val lengthKey   = "whatIsTradingName.error.businessName.length"
    val maxLength   = 105

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
