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

import forms.mappings.Mappings
import models.{Address, Country}
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

import javax.inject.Inject

class AddressUKFormProvider @Inject() extends Mappings with RegexConstants {

  val addressLineLength = 35

  def apply(countryList: Seq[Country]): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "addressUK.error.addressLine1.required",
        "addressUK.error.addressLine1.invalid",
        "addressUK.error.addressLine1.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine2" -> validatedOptionalText("addressUK.error.addressLine2.invalid",
                                              "addressUK.error.addressLine2.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "addressLine3" -> validatedText(
        "addressUK.error.addressLine3.required",
        "addressUK.error.addressLine3.invalid",
        "addressUK.error.addressLine3.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine4" -> validatedOptionalText("addressUK.error.addressLine4.invalid",
                                              "addressUK.error.addressLine4.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "postCode" -> addressPostcode("addressUK.error.postcode.invalid", regexPostcode, "addressUK.error.postcode.required"),
      "country" -> text("addressUK.error.country.required")
        .verifying("addressUK.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(Address.apply)(Address.unapply)
  )
}
