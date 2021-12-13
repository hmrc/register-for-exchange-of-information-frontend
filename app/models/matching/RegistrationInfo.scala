/*
 * Copyright 2021 HM Revenue & Customs
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

import models.matching.MatchingType.{AsIndividual, AsOrganisation}
import models.register.response.details.AddressResponse
import play.api.libs.json.{__, OFormat, OWrites, Reads}

case class RegistrationInfo(safeId: String, name: Option[String], address: Option[AddressResponse], matchedAs: MatchingType) {

  val isBusiness: Boolean   = matchedAs == AsOrganisation
  val isIndividual: Boolean = matchedAs == AsIndividual
}

object RegistrationInfo {

  import play.api.libs.functional.syntax._

  val reads: Reads[RegistrationInfo] =
    (
      (__ \ "safeId").read[String] and
        (__ \ "name").readNullable[String] and
        (__ \ "address").readNullable[AddressResponse] and
        (__ \ "matchedAs").read[MatchingType]
    )(RegistrationInfo.apply _)

  val writes: OWrites[RegistrationInfo] =
    (
      (__ \ "safeId").write[String] and
        (__ \ "name").writeNullable[String] and
        (__ \ "address").writeNullable[AddressResponse] and
        (__ \ "matchedAs").write[MatchingType]
    )(unlift(RegistrationInfo.unapply))

  implicit val format: OFormat[RegistrationInfo] = OFormat(reads, writes)
}
