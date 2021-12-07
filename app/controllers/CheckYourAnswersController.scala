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

import cats.data.EitherT
import cats.implicits._
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.BusinessType.Sole
import models.Regime
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.error.ApiError.{BadRequestError, EnrolmentExistsError, MandatoryInformationMissingError}
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{RegistrationService, SubscriptionService, TaxEnrolmentService}
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
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  taxEnrolmentsService: TaxEnrolmentService,
  countryFactory: CountryListFactory,
  registrationService: RegistrationService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging
    with WithEitherT {

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

  def onSubmit(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        registrationInfo   <- getEither(RegistrationInfoPage).orElse(EitherT(registrationService.registerWithoutId(regime)))
        subscriptionID     <- EitherT(subscriptionService.checkAndCreateSubscription(regime, registrationInfo.safeId, request.userAnswers))
        withSubscriptionID <- setEither(SubscriptionIDPage, subscriptionID)
        _ = sessionRepository.set(withSubscriptionID)
        _ <- EitherT(taxEnrolmentsService.checkAndCreateEnrolment(registrationInfo.safeId, withSubscriptionID, subscriptionID, regime))
      } yield Redirect(routes.RegistrationConfirmationController.onPageLoad(regime)))
        .valueOrF {
          case MandatoryInformationMissingError(error) =>
            Future.successful(Redirect(routes.SomeInformationIsMissingController.onPageLoad(regime)))
          case EnrolmentExistsError(_) =>
            if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithID(regime)))
            } else {
              Future.successful(Redirect(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutID(regime)))
            }
          case BadRequestError =>
            renderer
              .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
              .map(BadRequest(_))
          case _ =>
            renderer
              .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
              .map(InternalServerError(_))
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
