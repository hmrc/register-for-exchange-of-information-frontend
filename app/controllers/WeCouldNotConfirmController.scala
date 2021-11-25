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

import controllers.actions._
import models.{NormalMode, Regime, UserAnswers}
import pages.PageLists
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.Try

class WeCouldNotConfirmController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(key: String, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        val messages = implicitly[Messages]
        val data = Json.obj(
          "regime"   -> regime.toUpperCase,
          "affinity" -> messages(s"weCouldNotConfirm.$key"),
          "action"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode, regime).url
        )
        // clean invalid data
        clean(request.userAnswers)
        renderer.render("weCouldNotConfirm.njk", data).map(Ok(_))
    }

  private def clean(userAnswers: UserAnswers): Unit =
    PageLists.individualWithIdJourney.foldLeft(
      Try(userAnswers)
    )(PageLists.removePage)
}
