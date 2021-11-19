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

package models.register.request

import models.Name
import models.matching.RegistrationInfo
import models.register.request
import models.register.request.details.{PartnerDetails, WithIDIndividual, WithIDOrganisation}
import play.api.libs.json.{Json, OWrites, Reads, __}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class RequestWithIDDetails(
  IDType: String,
  IDNumber: String,
  requiresNameMatch: Boolean,
  isAnAgent: Boolean,
  partnerDetails: PartnerDetails
)

object RequestWithIDDetails {
  val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit lazy val requestWithIDDetailsReads: Reads[RequestWithIDDetails] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
        (__ \ "IDNumber").read[String] and
        (__ \ "requiresNameMatch").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "individual").readNullable[WithIDIndividual] and
        (__ \ "organisation").readNullable[WithIDOrganisation]
    )(
      (idType, idNumber, requiresNameMatch, isAnAgent, individual, organisation) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) => throw new Exception("Request details cannot have both and organisation or individual element")
          case (Some(ind), _)     => request.RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, ind)
          case (_, Some(org))     => request.RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, org)
          case (None, None)       => throw new Exception("Request Details must have either an organisation or individual element")
        }
    )
  }

  implicit lazy val requestWithIDDetailsWrites: OWrites[RequestWithIDDetails] = OWrites[RequestWithIDDetails] {
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, individual @ WithIDIndividual(_, _, _, _)) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "individual"        -> individual
      )
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, organisation @ WithIDOrganisation(_, _)) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "organisation"      -> organisation
      )
  }

  def apply(name: Name, dob: LocalDate, identifierName: String, identifierValue: String): RequestWithIDDetails =
    RequestWithIDDetails(
      identifierName,
      identifierValue,
      requiresNameMatch = true,
      isAnAgent = false, //This may change
      WithIDIndividual(name.firstName, None, name.lastName, dob.format(dateFormat))
    )

  def apply(registrationInfo: RegistrationInfo): RequestWithIDDetails =
    RequestWithIDDetails(
      registrationInfo.identifierType,
      registrationInfo.identifier.getOrElse(""),
      requiresNameMatch = true,
      isAnAgent = false, //This may change
      WithIDOrganisation(registrationInfo.name.getOrElse(""), registrationInfo.businessType.map(_.code).getOrElse(""))
    )
}
