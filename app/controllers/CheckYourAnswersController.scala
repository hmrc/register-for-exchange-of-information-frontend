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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, EnrolmentExistsError, MandatoryInformationMissingError}
import models.matching.RegistrationInfo
import models.{Regime, SubscriptionID}
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{RegistrationService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{CheckYourAnswersHelper, CountryListFactory}

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
    with Logging
    with WithEitherT {

  def onPageLoad(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      val helper                                = new CheckYourAnswersHelper(request.userAnswers, regime, countryListFactory = countryFactory)
      val businessDetails: Seq[SummaryList.Row] = helper.buildDetails(helper)

      val isBusiness = registrationService.isRegisteringAsBusiness()
      val (contactHeading, header) =
        if (isBusiness) ("firstContact", "businessDetails") else ("contactDetails", "individualDetails")

      renderer
        .render(
          "checkYourAnswers.njk",
          Json.obj(
            "regime"              -> regime.toUpperCase,
            "isBusiness"          -> isBusiness,
            "header"              -> s"checkYourAnswers.$header.h2",
            "contactHeading"      -> s"checkYourAnswers.$contactHeading.h2",
            "businessDetailsList" -> businessDetails,
            "firstContactList"    -> helper.buildFirstContact,
            "secondContactList"   -> helper.buildSecondContact,
            "action"              -> Navigator.checkYourAnswers(regime).url // todo change once backend for onSubmit is implemented
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
            renderer.render("thereIsAProblem.njk").map(BadRequest(_))
          case _ =>
            renderer.render("thereIsAProblem.njk").map(InternalServerError(_))
        }
  }
}
