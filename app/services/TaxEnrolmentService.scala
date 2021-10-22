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
import models.UserAnswers
import models.enrolment.SubscriptionInfo
import models.subscription.response.SubscriptionID
import org.slf4j.LoggerFactory
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentService @Inject() (taxEnrolmentsConnector: TaxEnrolmentsConnector) {

  private val logger = LoggerFactory.getLogger(getClass)

  def createEnrolment(userAnswers: UserAnswers, subscriptionId: SubscriptionID)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Int, Int]] =
    SubscriptionInfo.createSubscriptionInfo(userAnswers, subscriptionId.subscriptionID) match {
      case Right(subscriptionInfo: SubscriptionInfo) => taxEnrolmentsConnector.createEnrolment(subscriptionInfo).value
      case Left(throwable: Throwable) =>
        logger.warn(s"Cannot create subscription info ${throwable.getMessage}")
        Future.successful(Left(INTERNAL_SERVER_ERROR))
    }

}
