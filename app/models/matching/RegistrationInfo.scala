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
import models.{BusinessType, Name, UniqueTaxpayerReference}
import play.api.libs.json.{__, OFormat, OWrites, Reads}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

case class RegistrationInfo(safeId: String,
                            name: Option[String],
                            address: Option[AddressResponse],
                            matchedAs: MatchingType,
                            businessType: Option[BusinessType],
                            identifier: Option[String] = None,
                            dob: Option[LocalDate] = None
) {

  val isBusiness: Boolean   = matchedAs == AsOrganisation
  val isIndividual: Boolean = matchedAs == AsIndividual

  val identifierType: String = matchedAs match {
    case AsIndividual   => "NINO"
    case AsOrganisation => "UTR"
    case _              => "INVALID"
  }

  def sameAs(registrationInfo: RegistrationInfo): Boolean =
    safeId.nonEmpty &&
      businessType.equals(registrationInfo.businessType) &&
      name.map(_.toLowerCase).equals(registrationInfo.name.map(_.toLowerCase)) &&
      identifier.equals(registrationInfo.identifier) &&
      dob.equals(registrationInfo.dob)
}

object RegistrationInfo {

  import play.api.libs.functional.syntax._

  val reads: Reads[RegistrationInfo] =
    (
      (__ \ "safeId").read[String] and
        (__ \ "name").readNullable[String] and
        (__ \ "address").readNullable[AddressResponse] and
        (__ \ "matchedAs").read[MatchingType] and
        (__ \ "businessType").readNullable[BusinessType] and
        (__ \ "identifier").readNullable[String] and
        (__ \ "dob").readNullable[LocalDate]
    )(RegistrationInfo.apply _)

  val writes: OWrites[RegistrationInfo] =
    (
      (__ \ "safeId").write[String] and
        (__ \ "name").writeNullable[String] and
        (__ \ "address").writeNullable[AddressResponse] and
        (__ \ "matchedAs").write[MatchingType] and
        (__ \ "businessType").writeNullable[BusinessType] and
        (__ \ "identifier").writeNullable[String] and
        (__ \ "dob").writeNullable[LocalDate]
    )(unlift(RegistrationInfo.unapply))

  implicit val format: OFormat[RegistrationInfo] = OFormat(reads, writes)

  def apply(safeId: String,
            name: Option[String],
            address: Option[AddressResponse],
            matchedAs: MatchingType,
            businessType: Option[BusinessType],
            identifier: Option[String],
            dob: Option[LocalDate]
  ): RegistrationInfo = new RegistrationInfo(safeId, name, address, matchedAs, businessType, identifier, dob)

  def build(safeId: String, matchedAs: MatchingType): RegistrationInfo =
    RegistrationInfo(safeId, None, None, matchedAs, None, None, None)

  def build(name: Name, nino: Nino, dateOfBirth: Option[LocalDate]): RegistrationInfo =
    RegistrationInfo("", Option(name.fullName), None, AsIndividual, None, Option(nino.nino), dateOfBirth)

  def build(businessType: BusinessType, businessName: String, utr: UniqueTaxpayerReference, dateOfBirth: Option[LocalDate]): RegistrationInfo =
    RegistrationInfo("", Option(businessName), None, AsOrganisation, Option(businessType), Option(utr.uniqueTaxPayerReference), dateOfBirth)
}
