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

import com.google.inject.Inject
import connectors.TaxEnrolmentsConnector
import models.enrolment.SubscriptionInfo
import models.error.ApiError
import models.{Regime, UserAnswers}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentService @Inject() (taxEnrolmentsConnector: TaxEnrolmentsConnector) extends Logging {

  def createEnrolment(safeId: String, userAnswers: UserAnswers, subscriptionId: String, regime: Regime)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, Int]] =
    SubscriptionInfo.createSubscriptionInfo(safeId, userAnswers, subscriptionId) match {
      case Right(subscriptionInfo: SubscriptionInfo) =>
        taxEnrolmentsConnector.createEnrolment(subscriptionInfo, regime).value
      case Left(apiError: ApiError) =>
        logger.error("Could not create subscription info for enrolment missing Safe ID")
        Future.successful(Left(apiError))
    }
}
