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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.slf4j.LoggerFactory
import pages.ContactNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.CheckYourAnswersHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val helper                              = new CheckYourAnswersHelper(request.userAnswers)
      val firstContact: Seq[SummaryList.Row]  = buildFirstContact(helper)
      val secondContact: Seq[SummaryList.Row] = buildSecondContact(helper)

      renderer
        .render(
          "checkYourAnswers.njk",
          Json.obj(
            "firstContactList"  -> firstContact,
            "secondContactList" -> secondContact,
            "action"            -> "routes.CheckYourAnswersController.onSubmit().url"
          )
        )
        .map(Ok(_))
  }

  private def buildFirstContact(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple3(
      helper.contactName,
      helper.contactEmail,
      helper.contactPhone
    )

    pagesToCheck match {
      case (Some(_), Some(_), None) =>
        //No contact telephone
        Seq(
          helper.contactName,
          helper.contactEmail
        ).flatten
      case _ =>
        //All pages
        Seq(
          helper.contactName,
          helper.contactEmail,
          helper.contactPhone
        ).flatten
    }
  }

  private def buildSecondContact(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple4(
      helper.secondContact,
      helper.sndContactName,
      helper.sndContactEmail,
      helper.sndContactPhone
    )

    pagesToCheck match {
      case (Some(_), None, None, None) =>
        //No second contact
        Seq(
          helper.secondContact
        ).flatten
      case (Some(_), Some(_), Some(_), None) =>
        //No second contact phone
        Seq(
          helper.secondContact,
          helper.sndContactName,
          helper.sndContactEmail
        ).flatten
      case _ =>
        //All pages
        Seq(
          helper.secondContact,
          helper.sndContactName,
          helper.sndContactEmail,
          helper.sndContactPhone
        ).flatten
    }
  }
}
