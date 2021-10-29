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

package models.enrolment

import models.BusinessType.{LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models.UserAnswers
import pages.{AddressWithoutIdPage, BusinessTypePage, MatchingInfoPage, UTRPage, WhatIsYourNationalInsuranceNumberPage}
import play.api.libs.json.Json

import scala.util.Try

case class SubscriptionInfo(safeID: String,
                            saUtr: Option[String] = None,
                            ctUtr: Option[String] = None,
                            nino: Option[String] = None,
                            nonUkPostcode: Option[String] = None,
                            mdrId: String
) {

  def convertToEnrolmentRequest: EnrolmentRequest =
    EnrolmentRequest(identifiers = Seq(Identifier("MDRID", mdrId)), verifiers = buildVerifiers) //ToDo confirm MDRID as identifier

  def buildVerifiers: Seq[Verifier] = {

    val mandatoryVerifiers = Seq(Verifier("SAFEID", safeID))

    mandatoryVerifiers ++
      buildOptionalVerifier(saUtr, "SAUTR") ++
      buildOptionalVerifier(ctUtr, "CTUTR") ++
      buildOptionalVerifier(nino, "NINO") ++
      buildOptionalVerifier(nonUkPostcode, "NonUKPostalCode")

  }

  def buildOptionalVerifier(optionalInfo: Option[String], key: String): Seq[Verifier] =
    optionalInfo
      .map(
        info => Verifier(key, info)
      )
      .toSeq

}

object SubscriptionInfo {
  implicit val format = Json.format[SubscriptionInfo]

  def createSubscriptionInfo(userAnswers: UserAnswers, subscriptionId: String): SubscriptionInfo =
    SubscriptionInfo(
      safeID = getSafeID(userAnswers),
      saUtr = getSaUtrIfProvided(userAnswers),
      ctUtr = getCtUtrIfProvided(userAnswers),
      nino = getNinoIfProvided(userAnswers),
      nonUkPostcode = getNonUkPostCodeIfProvided(userAnswers),
      mdrId = subscriptionId
    )

  private def getSafeID(userAnswers: UserAnswers): String = userAnswers.get(MatchingInfoPage) match {
    case Some(matchInfo) => matchInfo.safeId
    case None            => throw new Exception("Safe ID can't be retrieved")
  }

  private def getNinoIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(WhatIsYourNationalInsuranceNumberPage) match {

      case Some(nino) => Some(nino.nino)
      case _          => None
    }

  private def getSaUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage) match {
      case Some(Partnership) | Some(Sole) | Some(LimitedPartnership) => userAnswers.get(UTRPage)
      case _                                                         => None
    }

  private def getCtUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => userAnswers.get(UTRPage)
      case _                                                      => None
    }

  private def getNonUkPostCodeIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(AddressWithoutIdPage) match {

      case Some(address) => address.postCode
      case _             => None
    }
}
