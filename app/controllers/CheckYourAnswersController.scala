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
import models.BusinessType.Sole
import models.Regime
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
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
import services.{BusinessMatchingWithoutIdService, EmailService, SubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CountryListFactory
import viewmodels.{CheckYourAnswersViewModel, Section}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  countryFactory: CountryListFactory,
  emailService: EmailService,
  controllerHelper: ControllerHelper,
  registrationService: BusinessMatchingWithoutIdService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def onPageLoad(regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      val viewModel: Seq[Section] =
        CheckYourAnswersViewModel.buildPages(request.userAnswers, regime, countryFactory, isRegisteringAsBusiness())

      renderer
        .render(
          "checkYourAnswers.njk",
          Json
            .obj(
              "regime"   -> regime.toUpperCase,
              "sections" -> viewModel,
              "action"   -> routes.CheckYourAnswersController.onSubmit(regime).url
            )
        )
        .map(Ok(_))
  }

  private def getSafeIdFromRegistration(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    request.userAnswers.get(RegistrationInfoPage) match {
      case Some(registration) =>
        val safeId = registration match {
          case OrgRegistrationInfo(safeId, _, _) => safeId
          case IndRegistrationInfo(safeId)       => safeId
        }
        Future.successful(Right(safeId))
      case _ => registrationService.registerWithoutId(regime)
    }

  def onSubmit(regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      (for {
        safeId         <- EitherT(getSafeIdFromRegistration(regime))
        subscriptionID <- EitherT(subscriptionService.checkAndCreateSubscription(regime, safeId, request.userAnswers))
        result         <- EitherT.right[ApiError](controllerHelper.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionID, regime))
        _              <- EitherT(emailService.sendAnLogEmail(request.userAnswers, subscriptionID))
      } yield result)
        .valueOrF {
          case EnrolmentExistsError(groupIds) if request.affinityGroup == AffinityGroup.Individual =>
            logger.info(s"EnrolmentExistsError for the the groupIds $groupIds")
            Future.successful(Redirect(routes.IndividualAlreadyRegisteredController.onPageLoad(regime)))
          case EnrolmentExistsError(groupIds) =>
            logger.info(s"EnrolmentExistsError for the the groupIds $groupIds")
            if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithID(regime)))
            } else {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutID(regime)))
            }
          case MandatoryInformationMissingError(_) =>
            Future.successful(Redirect(routes.SomeInformationIsMissingController.onPageLoad(regime)))
          case error => renderer.renderError(error, regime)
        }

  }

  private def isRegisteringAsBusiness()(implicit request: DataRequest[AnyContent]): Boolean =
    (request.userAnswers.get(WhatAreYouRegisteringAsPage),
     request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
     request.userAnswers.get(BusinessTypePage)
    ) match {
      case (None, Some(true), Some(Sole))                   => false
      case (None, Some(true), _)                            => true
      case (Some(RegistrationTypeBusiness), Some(false), _) => true
      case _                                                => false
    }
}
