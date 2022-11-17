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

import cats.data.EitherT
import cats.implicits._
import controllers.actions.StandardActionSets
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError}
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, SafeId}
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.{AuditService, BusinessMatchingWithoutIdService, SubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.{CountryListFactory, UserAnswersHelper}
import viewmodels.{CheckYourAnswersViewModel, Section}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  countryFactory: CountryListFactory,
  controllerHelper: ControllerHelper,
  registrationService: BusinessMatchingWithoutIdService,
  auditService: AuditService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging
    with UserAnswersHelper {

  def onPageLoad(): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val viewModel: Seq[Section] =
        CheckYourAnswersViewModel.buildPages(request.userAnswers, countryFactory, isRegisteringAsBusiness(request.userAnswers))

      renderer
        .render(
          "checkYourAnswers.njk",
          Json
            .obj(
              "sections" -> viewModel,
              "action"   -> routes.CheckYourAnswersController.onSubmit().url
            )
        )
        .map(Ok(_))
  }

  private def getSafeIdFromRegistration()(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    request.userAnswers.get(RegistrationInfoPage) match {
      case Some(registration) =>
        val safeId = registration match {
          case OrgRegistrationInfo(safeId, _, _) =>
            safeId
          case IndRegistrationInfo(safeId) =>
            safeId
        }
        Future.successful(Right(safeId))
      case _ =>
        registrationService.registerWithoutId()
    }

  def onSubmit(): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      (for {
        safeId         <- EitherT(getSafeIdFromRegistration())
        subscriptionID <- EitherT(subscriptionService.checkAndCreateSubscription(safeId, request.userAnswers))
        result         <- EitherT.right[ApiError](controllerHelper.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionID))
      } yield result)
        .valueOrF {
          case EnrolmentExistsError(groupIds) if request.affinityGroup == AffinityGroup.Individual =>
            logger.info(s"CheckYourAnswersController: EnrolmentExistsError for the groupIds $groupIds")
            Future.successful(Redirect(routes.IndividualAlreadyRegisteredController.onPageLoad()))
          case EnrolmentExistsError(groupIds) =>
            logger.info(s"CheckYourAnswersController: EnrolmentExistsError for the groupIds $groupIds")
            if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithId()))
            } else {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutId()))
            }
          case MandatoryInformationMissingError(_) =>
            logger.warn(s"CheckYourAnswersController: Mandatory information is missing")
            Future.successful(Redirect(routes.SomeInformationIsMissingController.onPageLoad()))
          case error => renderer.renderError(error)
        }

  }
}
