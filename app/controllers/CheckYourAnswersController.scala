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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, MandatoryInformationMissingError}
import models.requests.DataRequest
import pages.{DoYouHaveNINPage, DoYouHaveUniqueTaxPayerReferencePage, WhatAreYouRegisteringAsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckYourAnswersHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val helper = new CheckYourAnswersHelper(request.userAnswers)

      renderer
        .render(
          "checkYourAnswers.njk",
          Json.obj(
            "firstContactList"  -> helper.buildFirstContact,
            "secondContactList" -> helper.buildSecondContact,
            "action"            -> routes.CheckYourAnswersController.onSubmit().url
          )
        )
        .map(Ok(_))
  }

  private def createSubscription(userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    subscriptionService.createSubscription(userAnswers) flatMap {
      case Right(subscriptionID) =>
        logger.info(s"The subscriptionId id $subscriptionID")
        Future.successful(NotImplemented("Not implemented"))
      case Left(MandatoryInformationMissingError) => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      case Left(DuplicateSubmissionError) =>
        Future.successful(NotImplemented("DuplicateSubmission is not implemented")) //TODO create OrganisationHasAlreadyBeenRegistered page
      case Left(BadRequestError) => renderer.render("thereIsAProblem.njk").map(BadRequest(_))
      case _                     => renderer.render("thereIsAProblem.njk").map(InternalServerError(_))
    }

  def onSubmit(): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
       request.userAnswers.get(WhatAreYouRegisteringAsPage),
       request.userAnswers.get(DoYouHaveNINPage)
      ) match {
        case (Some(false), _, Some(false) | None) => Future.successful(NotImplemented("Not implemented")) // TODO DAC6-1142
        case (Some(_), _, Some(true) | None)      => createSubscription(request.userAnswers)
        case _                                    => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
