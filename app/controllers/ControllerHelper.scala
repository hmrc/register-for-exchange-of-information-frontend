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

package controllers

import models.error.ApiError.{EnrolmentExistsError, ServiceUnavailableError}
import models.{Regime, SubscriptionID, UserAnswers}
import models.requests.DataRequest
import pages.SubscriptionIDPage
import play.api.Logging
import play.api.mvc.Results.{InternalServerError, Redirect, ServiceUnavailable}
import play.api.mvc.{AnyContent, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.TaxEnrolmentService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ControllerHelper @Inject() (taxEnrolmentService: TaxEnrolmentService, renderer: Renderer, sessionRepository: SessionRepository)
    extends Logging
    with NunjucksSupport {

  private def createEnrolment(safeId: String, userAnswers: UserAnswers, subscriptionId: SubscriptionID, regime: Regime)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    taxEnrolmentService.checkAndCreateEnrolment(safeId, userAnswers, subscriptionId, regime) flatMap {
      case Right(_) => Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad(regime)))
      case Left(EnrolmentExistsError(groupIds)) =>
        logger.info(s"EnrolmentExistsError for the the groupIds $groupIds")
        Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithID(regime)))
      case Left(error) =>
        logger.info(s"Error $error received while creating an enrolment")
        renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
    }

  def updateSubscriptionIdAndCreateEnrolment(safeId: String, subscriptionId: SubscriptionID, regime: Regime)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionIDPage, subscriptionId))
      _              <- sessionRepository.set(updatedAnswers)
      result         <- createEnrolment(safeId, request.userAnswers, subscriptionId, regime)
    } yield result
}