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

import config.FrontendAppConfig
import controllers.actions._
import handlers.ErrorHandler
import models.Regime
import pages.SubscriptionIDPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  errorHandler: ErrorHandler,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      request.userAnswers.get(SubscriptionIDPage) match {
        case Some(id) =>
          val json = Json.obj(
            "regime"             -> regime.toString,
            "subscriptionID"     -> id.value,
            "submissionUrl"      -> appConfig.submissionsUrl,
            "betaFeedbackSurvey" -> appConfig.betaFeedbackUrl
          )
          renderer.render("registrationConfirmation.njk", json).map(Ok(_))
        case None =>
          errorHandler.onServerError(request, throw new RuntimeException("Subscription ID missing"))
      }
  }
}
