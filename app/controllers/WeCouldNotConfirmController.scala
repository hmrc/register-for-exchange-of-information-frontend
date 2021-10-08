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
import models.Mode
import org.slf4j.LoggerFactory
import pages.NotMatchingInfoPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WeCouldNotConfirmController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = LoggerFactory.getLogger(getClass)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val messages = implicitly[Messages]
      val affinity = request.userAnswers.get(NotMatchingInfoPage) match {
        case Some(key) => messages(s"weCouldNotConfirm.$key")
        case None =>
          logger.error("'We could not confirm' controller must be called with a valid answer.")
          throw new IllegalStateException("Either identity or business not matching info page is required.")
      }
      val data = Json.obj(
        "affinity" -> affinity,
        "action"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(mode).url
      )
      renderer.render("weCouldNotConfirm.njk", data).map(Ok(_))
  }
}
