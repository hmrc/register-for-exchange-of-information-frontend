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

import models.register.response.details.AddressResponse
import play.api.libs.json.{OFormat, OWrites, Reads, __}

sealed trait RegistrationInfo {
  val safeId: String
}

case class OrgRegistrationInfo(safeId: String, name: Option[String], address: Option[AddressResponse]) extends RegistrationInfo

object OrgRegistrationInfo {

  import play.api.libs.functional.syntax._

  val reads: Reads[OrgRegistrationInfo] =
    (
      (__ \ "safeId").read[String] and
        (__ \ "name").readNullable[String] and
        (__ \ "address").readNullable[AddressResponse]
    )(OrgRegistrationInfo.apply _)

  val writes: OWrites[OrgRegistrationInfo] =
    (
      (__ \ "safeId").write[String] and
        (__ \ "name").writeNullable[String] and
        (__ \ "address").writeNullable[AddressResponse]
    )(unlift(OrgRegistrationInfo.unapply))

  implicit val format: OFormat[OrgRegistrationInfo] = OFormat(reads, writes)
}

case class IndRegistrationInfo(safeId: String) extends RegistrationInfo

object IndRegistrationInfo {

  val reads: Reads[IndRegistrationInfo] =
    ((__ \ "safeId").read[String]).map(IndRegistrationInfo.apply)

  val writes: OWrites[IndRegistrationInfo] =
    ((__ \ "safeId").write[String]).contramap(_.safeId)

  implicit val format: OFormat[IndRegistrationInfo] = OFormat(reads, writes)
}
