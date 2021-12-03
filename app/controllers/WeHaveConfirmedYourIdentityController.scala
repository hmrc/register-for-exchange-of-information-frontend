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
import controllers.actions._
import models.error.ApiError
import models.error.ApiError.{BadRequestError, NotFoundError}
import models.matching.RegistrationInfo
import models.requests.DataRequest
import models.{BusinessType, CheckMode, Mode, NormalMode, Regime, UserAnswers}
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.{BusinessMatchingService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WeHaveConfirmedYourIdentityController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with WithEitherT {

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {

      implicit request =>
        val action: String = {
          if (containsContactDetails(request.userAnswers) && mode == CheckMode) routes.CheckYourAnswersController.onPageLoad(regime).url
          else {
            request.userAnswers.get(BusinessTypePage) match {
              case Some(BusinessType.Sole) => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
              case Some(_)                 => routes.ContactNameController.onPageLoad(NormalMode, regime).url
              case None                    => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
            }
          }
        }
        val json = Json.obj(
          "regime" -> regime.toUpperCase,
          "action" -> action
        )

        (for {
          registrationInfo <- EitherT(matchIndividualInfo(regime))
          updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
          _ = sessionRepository.set(updatedAnswers)
        } yield registrationInfo)
          .fold[Future[Result]](
            fa = {
              case NotFoundError =>
                Future.successful(Redirect(routes.WeCouldNotConfirmController.onPageLoad("identity", regime)))
              case _ =>
                renderer
                  .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
                  .map(ServiceUnavailable(_))
            },
            fb =>
              subscriptionService.getDisplaySubscriptionId(regime, fb.safeId) flatMap {
                case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(fb.safeId, subscriptionId, regime)
                case _                    => renderer.render("weHaveConfirmedYourIdentity.njk", json).map(Ok(_))
              }
          )
          .flatten

    }

  private def matchIndividualInfo(regime: Regime)(implicit request: DataRequest[AnyContent]): Future[Either[ApiError, RegistrationInfo]] =
    (for {
      nino             <- getEither(WhatIsYourNationalInsuranceNumberPage)
      name             <- getEither(WhatIsYourNamePage).orElse(getEither(SoleNamePage))
      dob              <- getEither(WhatIsYourDateOfBirthPage)
      registrationInfo <- EitherT(matchingService.sendIndividualRegistrationInformation(regime, RegistrationInfo.build(name, nino, Option(dob))))
    } yield registrationInfo).value

  // In CHECKMODE we check if contact details have been cleared down if not we can safely Redirec to CYA page
  private def containsContactDetails(ua: UserAnswers): Boolean = {
    val hasContactName = ua
      .get(ContactNamePage)
      .fold(false)(
        _ => true
      )
    val hasContactEmail = ua
      .get(ContactEmailPage)
      .fold(false)(
        _ => true
      )

    hasContactName || hasContactEmail
  }
}
