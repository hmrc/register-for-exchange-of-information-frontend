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
import models.BusinessType.{LimitedCompany, UnincorporatedAssociation}
import models.{NormalMode, Regime}
import pages.BusinessTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BusinessNotIdentifiedController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      val contactLink: String = request.userAnswers.get(BusinessTypePage) match {
        case Some(LimitedCompany) | Some(UnincorporatedAssociation) => appConfig.corporationTaxEnquiriesLink
        case _                                                      => appConfig.selfAssessmentEnquiriesLink
      }

      val data = Json.obj(
        "regime"     -> regime.toUpperCase,
        "contactUrl" -> contactLink,
        "lostUtrUrl" -> appConfig.lostUTRUrl,
        "startUrl"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode, regime).url
      )
      renderer.render("businessNotIdentified.njk", data).map(Ok(_))
  }
}