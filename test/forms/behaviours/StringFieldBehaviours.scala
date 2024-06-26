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

package forms.behaviours

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithMaxLengthAlpha(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThanAlpha(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithMaxLengthEmail(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(validEmailAddressToLong(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithMaxLengthName(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind names longer than $maxLength characters" in {

      forAll(validPersonalName(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithNonEmptyWhitespace(form: Form[_], fieldName: String, requiredError: FormError): Unit =
    s"must not bind strings of only whitespace" in {

      val result = form.bind(Map(fieldName -> " ")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

  def fieldWithFixedLengthNumeric(form: Form[_], fieldName: String, length: Int, lengthError: FormError): Unit =
    s"must not bind strings that are not $length characters" in {

      forAll(stringsNotOfFixedLengthNumeric(length) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithPostCodeRequired(
    form: Form[_],
    fieldName: String,
    countryCodeList: Seq[String],
    invalidError: FormError
  ): Unit =
    s"must not bind when postcode is required for a country" in {
      forAll(Gen.oneOf(countryCodeList)) { country =>
        val result = form.bind(Map("country" -> country)).apply(fieldName)
        result.errors.head mustEqual invalidError
      }
    }

  def fieldWithValidatedRegex(form: Form[_], fieldName: String, maxLength: Int, invalidError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters & invalid regex" in {

      forAll(stringsLongerThan(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustEqual Seq(invalidError)
      }
    }

  def fieldWithMaxLengthAndInvalid(form: Form[_], fieldName: String, maxLength: Int, errors: Seq[FormError]): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors shouldEqual errors
      }
    }
}
