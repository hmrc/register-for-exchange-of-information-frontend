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
import models.matching.RegistrationRequest
import models.requests.DataRequest
import models.{BusinessType, Mode, NormalMode, Regime}
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
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        val action: String =
          request.userAnswers.get(BusinessTypePage) match {
            case Some(BusinessType.Sole) => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
            case Some(_)                 => routes.ContactNameController.onPageLoad(NormalMode, regime).url
            case None                    => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
          }
        buildRegistrationRequest match {
          case Some(registrationRequest) =>
            matchingService
              .sendIndividualRegistrationInformation(regime, registrationRequest)
              .flatMap {
                case Right(info) =>
                  request.userAnswers.set(RegistrationInfoPage, info).map(sessionRepository.set)
                  subscriptionService.getDisplaySubscriptionId(regime, info.safeId) flatMap {
                    case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(info.safeId, subscriptionId, regime)
                    case _ =>
                      val json = Json.obj(
                        "regime" -> regime.toUpperCase,
                        "action" -> action
                      )
                      renderer.render("weHaveConfirmedYourIdentity.njk", json).map(Ok(_))
                  }
                case _ =>
                  Future.successful(Redirect(routes.WeCouldNotConfirmController.onPageLoad("identity", regime)))
              }
          case _ =>
            renderer
              .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
              .map(ServiceUnavailable(_))
        }

    }

  private def buildIndividualName(implicit request: DataRequest[AnyContent]): Option[String] =
    request.userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.Sole) => request.userAnswers.get(SoleNamePage).map(_.fullName)
      case _                       => request.userAnswers.get(WhatIsYourNamePage).map(_.fullName)
    }

  private def buildRegistrationRequest(implicit request: DataRequest[AnyContent]): Option[RegistrationRequest] =
    for {
      nino <- request.userAnswers.get(WhatIsYourNationalInsuranceNumberPage)
      name <- buildIndividualName
      dob  <- request.userAnswers.get(WhatIsYourDateOfBirthPage)
    } yield RegistrationRequest("NINO", nino.nino, name, None, Option(dob))
}
