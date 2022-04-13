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

package services

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.SubscriptionConnector
import models.audit.{AuditResponse, EventName, SubscriptionAudit}
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.SafeId
import models.subscription.request.{CreateSubscriptionForMDRRequest, DisplaySubscriptionRequest, SubscriptionRequest}
import models.{Regime, SubscriptionID, UserAnswers}
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (val subscriptionConnector: SubscriptionConnector, val auditService: AuditService) {

  private def auditCreateSubscriptionEvent(regime: Regime,
                                           userAnswers: UserAnswers,
                                           subscriptionRequest: SubscriptionRequest,
                                           response: Future[Either[ApiError, SubscriptionID]]
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[AuditResult] = {

    val auditResponse: Future[AuditResponse] = response map {
      case Right(subscriptionId) => AuditResponse("Success", Status.OK, Some(subscriptionId.value), None)
      case Left(value)           => AuditResponse("Failure", ApiError.convertToErrorCode(value), None, Some(value.toString))
    }

    for {
      response <- auditResponse
      details = Json.toJson(SubscriptionAudit.apply(userAnswers, subscriptionRequest.requestDetail, response))
      result <- auditService.sendAuditEvent(EventName.getEventName(regime), details)
    } yield result

  }

  def checkAndCreateSubscription(regime: Regime, safeID: SafeId, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, SubscriptionID]] =
    getDisplaySubscriptionId(regime, safeID) flatMap {
      case Some(subscriptionID) =>
        EitherT.rightT(subscriptionID).value
      case _ =>
        (SubscriptionRequest.convertTo(regime, safeID, userAnswers) match {
          case Some(subscriptionRequest) =>
            val response = subscriptionConnector.createSubscription(CreateSubscriptionForMDRRequest(subscriptionRequest))

            auditCreateSubscriptionEvent(regime, userAnswers, subscriptionRequest, response.value)

            response
          case _ =>
            EitherT.leftT(MandatoryInformationMissingError())
        }).value
    }

  def getDisplaySubscriptionId(regime: Regime, safeId: SafeId)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[SubscriptionID]] = {
    val displaySubscription: DisplaySubscriptionRequest = DisplaySubscriptionRequest.convertTo(regime, safeId.value)
    subscriptionConnector.readSubscription(displaySubscription)
  }
}
