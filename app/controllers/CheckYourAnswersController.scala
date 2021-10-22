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
import handlers.ErrorHandler
import models.BusinessType.Sole
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, MandatoryInformationMissingError}
import models.requests.DataRequest
import models.{Regime, UserAnswers, WhatAreYouRegisteringAs}
import navigation.Navigator
import pages.{BusinessTypePage, DoYouHaveNINPage, DoYouHaveUniqueTaxPayerReferencePage, WhatAreYouRegisteringAsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import services.{SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{CheckYourAnswersHelper, CountryListFactory}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  taxEnrolmentsService: TaxEnrolmentService,
  countryFactory: CountryListFactory,
  errorHandler: ErrorHandler,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def onPageLoad(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      val helper                                = new CheckYourAnswersHelper(request.userAnswers, regime, countryListFactory = countryFactory)
      val businessDetails: Seq[SummaryList.Row] = helper.buildDetails(helper)

      val contactHeading = if (getRegisteringAsBusiness()) "checkYourAnswers.firstContact.h2" else "checkYourAnswers.contactDetails.h2"

      val header: String =
        (request.userAnswers.get(BusinessTypePage), request.userAnswers.get(WhatAreYouRegisteringAsPage)) match {
          case (Some(_), _)                                                => "checkYourAnswers.businessDetails.h2"
          case (_, Some(WhatAreYouRegisteringAs.RegistrationTypeBusiness)) => "checkYourAnswers.businessDetails.h2"
          case _                                                           => "checkYourAnswers.individualDetails.h2"
        }

      renderer
        .render(
          "checkYourAnswers.njk",
          Json.obj(
            "header"              -> header,
            "regime"              -> regime.toUpperCase,
            "contactHeading"      -> contactHeading,
            "isBusiness"          -> getRegisteringAsBusiness(),
            "businessDetailsList" -> businessDetails,
            "firstContactList"    -> helper.buildFirstContact,
            "secondContactList"   -> helper.buildSecondContact,
            "action"              -> Navigator.checkYourAnswers(regime).url // todo change once backend for onSubmit is implemented
          )
        )
        .map(Ok(_))
  }

  private def getRegisteringAsBusiness()(implicit request: DataRequest[AnyContent]): Boolean =
    (request.userAnswers.get(WhatAreYouRegisteringAsPage),
     request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
     request.userAnswers.get(BusinessTypePage)
    ) match { //ToDo defaulting to registering for business change when paths created if necessary
      case (None, Some(true), Some(Sole))                   => false
      case (None, Some(true), _)                            => true
      case (Some(RegistrationTypeBusiness), Some(false), _) => true
      case _                                                => false
    }

  private def createSubscription(regime: Regime, userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    subscriptionService.createSubscription(userAnswers) flatMap {
      case Right(subscriptionID) =>
        logger.info(s"The subscriptionId id $subscriptionID") //ToDo enrolment here
        taxEnrolmentsService.createEnrolment(userAnswers, subscriptionID) flatMap {
          case Right(_) => Future.successful(NotImplemented("Not implemented")) //ToDo put in correct success route
          case Left(errorStatus) =>
            errorHandler.onClientError(request, errorStatus)
        }
      case Left(MandatoryInformationMissingError) => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad(regime, None)))
      case Left(DuplicateSubmissionError) =>
        Future.successful(NotImplemented("DuplicateSubmission is not implemented")) //TODO create OrganisationHasAlreadyBeenRegistered page
      case Left(BadRequestError) => errorHandler.onClientError(request, BAD_REQUEST)
      case _                     => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
    }

  def onSubmit(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
       request.userAnswers.get(WhatAreYouRegisteringAsPage),
       request.userAnswers.get(DoYouHaveNINPage)
      ) match {
        case (Some(false), _, Some(false) | None) => Future.successful(NotImplemented("Not implemented")) // TODO DAC6-1142
        case (Some(_), _, Some(true) | None)      => createSubscription(regime, request.userAnswers)
        case _                                    => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad(regime, None)))
      }
  }
}
