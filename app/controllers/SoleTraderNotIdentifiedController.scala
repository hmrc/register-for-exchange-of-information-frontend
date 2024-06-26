/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ReporterType.Sole
import pages.ReporterTypePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SoleTraderNotIdentifiedView

import javax.inject.Inject

class SoleTraderNotIdentifiedController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: SoleTraderNotIdentifiedView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = standardActionSets.identifiedUserWithData() { implicit request =>
    val startUrl = routes.IndexController.onPageLoad().url

    request.userAnswers.get(ReporterTypePage) match {
      case Some(Sole)   => Ok(view(startUrl))
      case reporterType =>
        logger.error(s"$reporterType reporter type is not eligible to view SoleTraderNotIdentifiedPage")
        Redirect(controllers.routes.ThereIsAProblemController.onPageLoad())
    }
  }
}
