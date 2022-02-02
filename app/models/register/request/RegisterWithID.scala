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

import models.matching.RegistrationRequest
import models.{Name, Regime}
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class RegisterWithID(registerWithIDRequest: RegisterWithIDRequest)

object RegisterWithID {
  implicit val format: Format[RegisterWithID] = Json.format[RegisterWithID]

  def apply(regime: Regime, name: Name, dob: Option[LocalDate], identifierName: String, identifierValue: String): RegisterWithID =
    RegisterWithID(
      RegisterWithIDRequest(
        RequestCommon(regime.toUpperCase),
        RequestWithIDDetails(name, dob, identifierName, identifierValue)
      )
    )

  def apply(regime: Regime, registrationRequest: RegistrationRequest): RegisterWithID =
    RegisterWithID(
      RegisterWithIDRequest(
        RequestCommon(regime.toUpperCase),
        RequestWithIDDetails(registrationRequest)
      )
    )
}
