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

package models.subscription.request

import models.{BusinessType, UserAnswers}
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import pages._
import play.api.libs.json.{__, Json, OWrites, Reads}

case class CreateRequestDetail(IDType: String,
                               IDNumber: String,
                               tradingName: Option[String],
                               isGBUser: Boolean,
                               primaryContact: PrimaryContact,
                               secondaryContact: Option[SecondaryContact]
)

object CreateRequestDetail {

  implicit val reads: Reads[CreateRequestDetail] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
        (__ \ "IDNumber").read[String] and
        (__ \ "tradingName").readNullable[String] and
        (__ \ "isGBUser").read[Boolean] and
        (__ \ "primaryContact").read[PrimaryContact] and
        (__ \ "secondaryContact").readNullable[SecondaryContact]
    )(
      (idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact) =>
        CreateRequestDetail(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
    )
  }

  implicit val writes: OWrites[CreateRequestDetail] = Json.writes[CreateRequestDetail]
  private val idType: String                        = "SAFE"

  def convertTo(safeId: String, userAnswers: UserAnswers): Option[CreateRequestDetail] = {

    val individualOrSoleTrader = {
      (userAnswers.get(WhatAreYouRegisteringAsPage), userAnswers.get(BusinessTypePage)) match {
        case (Some(RegistrationTypeIndividual), _) => true
        case (_, Some(BusinessType.Sole))          => true
        case _                                     => false
      }
    }
    val secondContact = if (individualOrSoleTrader) { Right(None) }
    else { SecondaryContact.convertTo(userAnswers) }

    secondContact match {
      case Right(secondContact) =>
        for {
          primaryContact <- PrimaryContact.convertTo(userAnswers)
        } yield CreateRequestDetail(
          IDType = idType,
          IDNumber = safeId,
          tradingName = userAnswers.get(WhatIsTradingNamePage),
          isGBUser = isGBUser(userAnswers),
          primaryContact = primaryContact,
          secondaryContact = secondContact
        )
      case _ => None
    }
  }

  private def isGBUser(userAnswers: UserAnswers): Boolean =
    userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage) match {
      case Some(true) => true
      case _ =>
        userAnswers.get(WhatAreYouRegisteringAsPage) match {
          case Some(RegistrationTypeIndividual) =>
            userAnswers.get(DoYouHaveNINPage) match {
              case Some(true) => true
              case _ =>
                userAnswers.get(DoYouLiveInTheUKPage).contains(true)
            }
          case Some(RegistrationTypeBusiness) =>
            userAnswers.get(AddressWithoutIdPage).exists(_.isGB)
          case _ => false
        }
    }
}
