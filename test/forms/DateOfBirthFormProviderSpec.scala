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

import forms.behaviours.DateBehaviours
import play.api.data.Form

import java.time.{LocalDate, ZoneOffset}

class DateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new DateOfBirthFormProvider()()

  val twoThousand = 2000
  val year        = 1908

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(twoThousand, one, one),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value.day", "dateOfBirth.error.required.all")

    "must return a FormError when month is missing" in {
      val key  = "value"
      val date = LocalDate.now()

      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> "",
        s"$key.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors.size mustBe one
      result.errors.head.message mustBe "dateOfBirth.error.required"
      result.errors.head.args mustBe Seq("month")
    }

    "must return a FormError when month is invalid" in {
      val key  = "value"
      val date = LocalDate.now()

      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> "a",
        s"$key.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors.size mustBe one
      result.errors.head.message mustBe "dateOfBirth.error.invalid"
      result.errors.head.args mustBe Seq("month")
    }

    "must not allow a date later than today" in {
      val key  = "value"
      val date = LocalDate.now().plusYears(one)
      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> date.getMonthValue.toString,
        s"$key.year"  -> date.getYear.toString
      )

      val result: Form[LocalDate] = form.bind(data)

      result.errors.size mustBe one
      result.errors.head.message mustBe "dateOfBirth.error.futureDate"
    }

    "must not allow a date earlier than 01/01/1909" in {
      val key  = "value"
      val date = LocalDate.of(year, one, one)
      val data = Map(
        s"$key.day"   -> date.getDayOfMonth.toString,
        s"$key.month" -> date.getMonthValue.toString,
        s"$key.year"  -> date.getYear.toString
      )

      val result: Form[LocalDate] = form.bind(data)

      result.errors.size mustBe one
      result.errors.head.message mustBe "dateOfBirth.error.pastDate"
    }

  }
}
