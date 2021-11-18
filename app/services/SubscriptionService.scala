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

package services

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.SubscriptionConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.subscription.request.{CreateSubscriptionForMDRRequest, DisplaySubscriptionRequest, SubscriptionRequest}
import models.{Regime, SubscriptionID, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector) {

  def checkAndCreateSubscription(regime: Regime, safeID: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, SubscriptionID]] = {
    val displaySubscription = DisplaySubscriptionRequest.convertTo(regime, safeID, userAnswers)
    subscriptionConnector.readSubscription(displaySubscription) flatMap {
      case Some(subscriptionID) =>
        EitherT.rightT(subscriptionID).value
      case _ =>
        (SubscriptionRequest.convertTo(regime, safeID, userAnswers) match {
          case Some(subscriptionRequest) =>
            subscriptionConnector
              .createSubscription(CreateSubscriptionForMDRRequest(subscriptionRequest))
          case _ => EitherT.leftT(MandatoryInformationMissingError())
        }).value
    }

  }

}
