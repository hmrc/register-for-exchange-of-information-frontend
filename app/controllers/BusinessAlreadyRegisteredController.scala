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
import models.Regime
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BusinessAlreadyRegisteredController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  frontendAppConfig: FrontendAppConfig,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoadWithID(regime: Regime): Action[AnyContent] = (identify(regime)).async {
    implicit request =>
      val json = Json.obj(
        "withID"       -> true,
        "emailAddress" -> frontendAppConfig.emailEnquiries
      )

      renderer.render("businessAlreadyRegistered.njk", json).map(Ok(_))
  }

  def onPageLoadWithoutID(regime: Regime): Action[AnyContent] = (identify(regime)).async {
    implicit request =>
      val json = Json.obj(
        "withID"       -> false,
        "emailAddress" -> frontendAppConfig.emailEnquiries,
        "loginGG"      -> frontendAppConfig.loginUrl
      )

      renderer.render("businessAlreadyRegistered.njk", json).map(Ok(_))
  }

}
