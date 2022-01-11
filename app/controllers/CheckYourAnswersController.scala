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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.BusinessType.Sole
import models.Regime
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError}
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{RegistrationService, SubscriptionService, TaxEnrolmentService}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.auth.core.AffinityGroup

import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CountryListFactory
import viewmodels.{CheckYourAnswersViewModel, Section}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  taxEnrolmentsService: TaxEnrolmentService,
  countryFactory: CountryListFactory,
  registrationService: RegistrationService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def onPageLoad(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
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

  private def buildRegistrationInfo(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier) =
    request.userAnswers.getEither(RegistrationInfoPage) match {
      case Left(_)      => EitherT(registrationService.registerWithoutId(regime))
      case registration => EitherT.fromEither[Future](registration)
    }

  def onSubmit(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        registrationInfo   <- buildRegistrationInfo(regime)
        subscriptionID     <- EitherT(subscriptionService.checkAndCreateSubscription(regime, registrationInfo.safeId, request.userAnswers))
        withSubscriptionID <- EitherT.fromEither[Future](request.userAnswers.setEither(SubscriptionIDPage, subscriptionID))
        _                  <- EitherT.fromEither[Future](Right[ApiError, Future[Boolean]](sessionRepository.set(withSubscriptionID)))
        _                  <- EitherT(taxEnrolmentsService.checkAndCreateEnrolment(registrationInfo.safeId, withSubscriptionID, subscriptionID, regime))
      } yield Redirect(routes.RegistrationConfirmationController.onPageLoad(regime)))
        .valueOrF {
          case MandatoryInformationMissingError(error) =>
            Future.successful(Redirect(routes.SomeInformationIsMissingController.onPageLoad(regime)))
          case EnrolmentExistsError(_) if request.affinityGroup == AffinityGroup.Individual =>
            Future.successful(Redirect(routes.IndividualAlreadyRegisteredController.onPageLoad(regime)))
          case EnrolmentExistsError(_) =>
            if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithID(regime)))
            } else {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutID(regime)))
            }
          case error =>
            renderer
              .renderError(error, regime)
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
