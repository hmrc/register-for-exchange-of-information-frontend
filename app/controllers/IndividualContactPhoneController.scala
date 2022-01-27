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

import controllers.actions._
import forms.IndividualContactPhoneFormProvider

import javax.inject.Inject
import models.{Mode, Regime}
import models.requests.DataRequest
import navigation.{ContactDetailsNavigator, Navigator}
import pages.IndividualContactPhonePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels._

import scala.concurrent.{ExecutionContext, Future}

class IndividualContactPhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: IndividualContactPhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "action" -> routes.IndividualContactPhoneController.onSubmit(mode, regime).url
    )
    renderer.render("individualContactPhone.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      render(mode, regime, request.userAnswers.get(IndividualContactPhonePage).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualContactPhonePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IndividualContactPhonePage, mode, regime, updatedAnswers))
        )
  }
}
