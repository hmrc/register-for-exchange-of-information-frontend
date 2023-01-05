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

package forms

import forms.mappings.Mappings
import models.NonUkName
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

import javax.inject.Inject

class NonUkNameFormProvider @Inject() extends Mappings with RegexConstants {

  private val maxLength = 35

  def apply(): Form[NonUkName] = Form(
    mapping(
      "givenName" -> validatedText(
        "nonUkName.error.givenName.required",
        "nonUkName.error.givenName.invalid",
        "nonUkName.error.givenName.length",
        individualNameRegex,
        maxLength
      ),
      "familyName" -> validatedText(
        "nonUkName.error.familyName.required",
        "nonUkName.error.familyName.invalid",
        "nonUkName.error.familyName.length",
        individualNameRegex,
        maxLength
      )
    )(NonUkName.apply)(NonUkName.unapply)
  )
}
