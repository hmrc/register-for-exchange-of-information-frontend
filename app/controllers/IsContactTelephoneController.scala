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
import forms.IsContactTelephoneFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ContactDetailsNavigator
import pages.{ContactNamePage, IsContactTelephonePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.UserAnswersHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsContactTelephoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: IsContactTelephoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with UserAnswersHelper {

  private val form = formProvider()

  private def data(mode: Mode, form: Form[Boolean])(implicit request: DataRequest[AnyContent]): JsObject = {
    val name       = request.userAnswers.get(ContactNamePage)
    val filledForm = request.userAnswers.get(IsContactTelephonePage).fold(form)(form.fill)
    Json.obj(
      "form"      -> filledForm,
      "name"      -> name,
      "pageTitle" -> s"isContactTelephone.title.business",
      "heading"   -> s"isContactTelephone.heading.business",
      "action"    -> routes.IsContactTelephoneController.onSubmit(mode).url,
      "radios"    -> Radios.yesNo(filledForm("value"))
    )
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        renderer.render("isContactTelephone.njk", data(mode, form)).map(Ok(_))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => renderer.render("isContactTelephone.njk", data(mode, formWithErrors)).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsContactTelephonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsContactTelephonePage, mode, updatedAnswers))
          )
    }
}
