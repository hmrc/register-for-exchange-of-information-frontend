/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class NonUkNameFormProviderSpec extends StringFieldBehaviours {

  val form      = new NonUkNameFormProvider()()
  val maxLength = 35

  ".givenName" - {

    val fieldName   = "givenName"
    val requiredKey = "nonUkName.error.givenName.required"
    val lengthKey   = "nonUkName.error.givenName.length"
    val invalidKey  = "nonUkName.error.givenName.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(individualNameRegex),
      errorToFind = Some(invalidKey)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq())
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jfhf-\\^' `&%",
      FormError(fieldName, invalidKey)
    )

  }

  ".familyName" - {

    val fieldName   = "familyName"
    val requiredKey = "nonUkName.error.familyName.required"
    val lengthKey   = "nonUkName.error.familyName.length"
    val invalidKey  = "nonUkName.error.familyName.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(individualNameRegex),
      errorToFind = Some(invalidKey)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq())
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jfhf-\\^' `&%",
      FormError(fieldName, invalidKey)
    )
  }
}
