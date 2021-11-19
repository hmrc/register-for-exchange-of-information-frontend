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

import cats.implicits._
import controllers.actions._
import models.{BusinessType, NormalMode, Regime}
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WeHaveConfirmedYourIdentityController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with WithEitherT {

  def onPageLoad(regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        val action: String = request.userAnswers.get(BusinessTypePage) match {
          case Some(BusinessType.Sole) => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
          case Some(_)                 => routes.ContactNameController.onPageLoad(NormalMode, regime).url
          case None                    => routes.ContactEmailController.onPageLoad(NormalMode, regime).url
        }

        // todo wogole pozbyc sie for i sam json i chyba nie potrzebuje thereIsAproblem bo juz nie robie tu porownania to jest
        // todo tylko pass by Controller
        (for {
          registrationInfo <- getEither(RegistrationInfoPage)
          updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
          _ = sessionRepository.set(updatedAnswers)
        } yield Json.obj(
          "regime" -> regime.toUpperCase,
          "action" -> action
        )).semiflatMap {
          json => renderer.render("weHaveConfirmedYourIdentity.njk", json).map(Ok(_))
        }.valueOrF(
          _ => renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
        )
    }
}
