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

package models.register.request

import models.Regime.MDR
import models.register.request.details.{AddressRequest, Individual, NoIdOrganisation}
import models.shared.ContactDetails
import models.{Address, Name}
import play.api.libs.json.Json

import java.time.LocalDate

case class RegisterWithoutID(
  registerWithoutIDRequest: RegisterWithoutIDRequest
)

object RegisterWithoutID {

  implicit val format = Json.format[RegisterWithoutID]

  def apply(name: Name, dob: LocalDate, address: Address, contactDetails: ContactDetails): RegisterWithoutID =
    RegisterWithoutID(
      RegisterWithoutIDRequest(
        RequestCommon(MDR.toString),
        RequestWithoutIDDetails(None, Option(Individual(name, dob)), AddressRequest(address), contactDetails, None)
      )
    )

  def apply(organisationName: String, address: Address, contactDetails: ContactDetails): RegisterWithoutID =
    RegisterWithoutID(
      RegisterWithoutIDRequest(
        RequestCommon(MDR.toString),
        RequestWithoutIDDetails(Option(organisationName).map(NoIdOrganisation(_)), None, AddressRequest(address), contactDetails, None)
      )
    )
}
