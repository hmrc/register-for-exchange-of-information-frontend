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

package models.audit

import models.subscription.request.{ContactInformation, SubscriptionRequest}
import models.{BusinessType, UserAnswers}
import pages.{BusinessTypePage, IsThisYourBusinessPage, UTRPage, WhatIsYourNationalInsuranceNumberPage}
import play.api.libs.json.{Json, OFormat}

case class SubscriptionAudit(
  SAFEID: String,
  UTR: String,
  NINO: String,
  saUTR: String,
  isBusiness: Boolean = false,
  tradingName: Option[String],
  isGBUser: Boolean,
  primaryContact: ContactInformation,
  secondaryContact: Option[ContactInformation],
  response: AuditResponse
)

object SubscriptionAudit {

  def apply(userAnswers: UserAnswers, subscriptionRequest: SubscriptionRequest, auditResponse: AuditResponse): SubscriptionAudit = {
    val (utr, saUtr) = {
      userAnswers.get(BusinessTypePage) match {
        case Some(BusinessType.Sole) => (None, userAnswers.get(UTRPage))
        case Some(x: BusinessType)   => (userAnswers.get(UTRPage), None)
        case None                    => (None, None)
      }
    }

    SubscriptionAudit(
      SAFEID = subscriptionRequest.requestDetail.IDNumber,
      UTR = utr.fold("_")(_.uniqueTaxPayerReference),
      NINO = userAnswers.get(WhatIsYourNationalInsuranceNumberPage).fold("_")(_.nino),
      saUTR = saUtr.fold("_")(_.uniqueTaxPayerReference),
      isBusiness = userAnswers.get(IsThisYourBusinessPage).getOrElse(false),
      tradingName = subscriptionRequest.requestDetail.tradingName,
      isGBUser = subscriptionRequest.requestDetail.isGBUser,
      primaryContact = subscriptionRequest.requestDetail.primaryContact,
      secondaryContact = subscriptionRequest.requestDetail.secondaryContact,
      response = auditResponse
    )
  }

  implicit val format: OFormat[SubscriptionAudit] = Json.format[SubscriptionAudit]
}
