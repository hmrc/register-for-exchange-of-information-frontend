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

package models.subscription.request

import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.{BusinessType, UserAnswers}
import pages._
import play.api.libs.json.{Json, OFormat}

case class CreateRequestDetail(IDType: String,
                               IDNumber: String,
                               tradingName: Option[String],
                               isGBUser: Boolean,
                               primaryContact: PrimaryContact,
                               secondaryContact: Option[SecondaryContact]
)

object CreateRequestDetail {

  implicit val format: OFormat[CreateRequestDetail] = Json.format[CreateRequestDetail]
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
