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
import models.DateHelper.today
import play.api.data.Form

import java.time.LocalDate
import javax.inject.Inject

class DateOfBirthFormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "dateOfBirth.error.invalid",
        allRequiredKey = "dateOfBirth.error.required.all",
        twoRequiredKey = "dateOfBirth.error.required.two",
        requiredKey = "dateOfBirth.error.required",
        maxDateKey = "dateOfBirth.error.futureDate",
        minDateKey = "dateOfBirth.error.pastDate",
        maxDate = today,
        minDate = LocalDate.of(1909, 1, 1)
      )
    )
}
