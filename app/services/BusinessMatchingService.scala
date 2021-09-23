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

import models.matching.MatchingInfo
import models.subscription.BusinessDetails
import models.subscription.response.DisplaySubscriptionForCBCResponse
import models.{UserAnswers, WhatIsYourName}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() () {

  def sendIndividualMatchingInformation(nino: String, name: Option[WhatIsYourName], dob: Option[LocalDate])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[Exception, MatchingInfo]] =
    Future.successful(Left(new RuntimeException)) // TODO implement

  def sendBusinessMatchingInformation(userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[(Option[BusinessDetails], Option[String], Option[DisplaySubscriptionForCBCResponse])] =
    Future.failed(new RuntimeException) // TODO implement
}
