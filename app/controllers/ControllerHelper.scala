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

package controllers

import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError}
import models.matching.SafeId
import models.requests.DataRequest
import models.{SubscriptionID, UserAnswers}
import pages.{RegistrationInfoPage, SubscriptionIDPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.TaxEnrolmentService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ControllerHelper @Inject() (taxEnrolmentService: TaxEnrolmentService, renderer: Renderer, sessionRepository: SessionRepository)
    extends Logging
    with NunjucksSupport {

  private def createEnrolment(safeId: SafeId, userAnswers: UserAnswers, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    taxEnrolmentService.checkAndCreateEnrolment(safeId, userAnswers, subscriptionId) flatMap {
      case Right(_) => Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad()))
      case Left(EnrolmentExistsError(groupIds)) if request.affinityGroup == AffinityGroup.Individual =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        Future.successful(Redirect(routes.IndividualAlreadyRegisteredController.onPageLoad()))
      case Left(EnrolmentExistsError(groupIds)) =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
          Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithId()))
        } else {
          Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutId()))
        }
      case Left(MandatoryInformationMissingError(_)) =>
        logger.warn(s"ControllerHelper: Mandatory information is missing")
        Future.successful(Redirect(routes.SomeInformationIsMissingController.onPageLoad()))
      case Left(error) => renderer.renderError(error)
    }

  def updateSubscriptionIdAndCreateEnrolment(safeId: SafeId, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionIDPage, subscriptionId))
      _              <- sessionRepository.set(updatedAnswers)
      result         <- createEnrolment(safeId, request.userAnswers, subscriptionId)
    } yield result
}
