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

import config.FrontendAppConfig
import controllers.actions._
import models.error.ApiError.NotFoundError
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{Mode, Regime, UserAnswers}
import navigation.MDRNavigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{BusinessMatchingWithIdService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WeHaveConfirmedYourIdentityController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  standardActionSets: StandardActionSets,
  val appConfig: FrontendAppConfig,
  navigator: MDRNavigator,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  val renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        buildRegisterWithID(regime) match {
          case Some(registrationRequest) =>
            matchingService
              .sendIndividualRegistrationInformation(registrationRequest)
              .flatMap {
                case Right(info) =>
                  request.userAnswers.set(RegistrationInfoPage, info).map(sessionRepository.set)
                  subscriptionService.getDisplaySubscriptionId(regime, info.safeId) flatMap {
                    case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(info.safeId, subscriptionId, regime)
                    case _ =>
                      val json = Json.obj(
                        "regime" -> regime.toUpperCase,
                        "action" -> navigator.nextPage(RegistrationInfoPage, mode, regime, request.userAnswers).url
                      )
                      renderer.render("weHaveConfirmedYourIdentity.njk", json).map(Ok(_))
                  }
                case Left(NotFoundError) =>
                  Future.successful(Redirect(routes.WeCouldNotConfirmController.onPageLoad("identity", regime)))
                case _ =>
                  renderer.renderThereIsAProblemPage(regime)
              }
          case _ =>
            renderer.renderThereIsAProblemPage(regime)
        }
    }

  private def buildRegisterWithID(regime: Regime)(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      nino <- request.userAnswers.get(WhatIsYourNationalInsuranceNumberPage)
      name <- request.userAnswers.get(WhatIsYourNamePage)
      dob  <- request.userAnswers.get(WhatIsYourDateOfBirthPage)
    } yield RegisterWithID(regime, name, Some(dob), "NINO", nino.nino)
}
