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

package models.matching

import models.BusinessType
import play.api.libs.json.{__, OFormat, OWrites, Reads}

import java.time.LocalDate

case class RegistrationRequest(identifierType: String,
                               identifier: String,
                               name: String,
                               businessType: Option[BusinessType] = None,
                               dob: Option[LocalDate] = None
) {

  def sameAs(registrationRequest: RegistrationRequest): Boolean =
    identifier.equals(registrationRequest.identifier) &&
      name.toLowerCase.equals(registrationRequest.name.toLowerCase) &&
      businessType.equals(registrationRequest.businessType) &&
      dob.equals(registrationRequest.dob)
}

object RegistrationRequest {

  import play.api.libs.functional.syntax._

  val reads: Reads[RegistrationRequest] =
    (
      (__ \ "identifierType").read[String] and
        (__ \ "identifier").read[String] and
        (__ \ "name").read[String] and
        (__ \ "businessType").readNullable[BusinessType] and
        (__ \ "dob").readNullable[LocalDate]
    )(RegistrationRequest.apply _)

  val writes: OWrites[RegistrationRequest] =
    (
      (__ \ "identifierType").write[String] and
        (__ \ "identifier").write[String] and
        (__ \ "name").write[String] and
        (__ \ "businessType").writeNullable[BusinessType] and
        (__ \ "dob").writeNullable[LocalDate]
    )(unlift(RegistrationRequest.unapply))

  implicit val format: OFormat[RegistrationRequest] = OFormat(reads, writes)
}
