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
import forms.SndConHavePhoneFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ContactDetailsNavigator
import pages.{SndConHavePhonePage, SndContactNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SndConHavePhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: SndConHavePhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[Boolean], name: String)(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "name"   -> name,
      "action" -> routes.SndConHavePhoneController.onSubmit(mode).url,
      "radios" -> Radios.yesNo(form("value"))
    )
    renderer.render("sndConHavePhone.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(SndContactNamePage).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(SndConHavePhonePage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        render(mode, preparedForm, request.userAnswers.get(SndContactNamePage).get).map(Ok(_))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(SndContactNamePage).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, formWithErrors, request.userAnswers.get(SndContactNamePage).get).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SndConHavePhonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SndConHavePhonePage, mode, updatedAnswers))
          )
    }
}
