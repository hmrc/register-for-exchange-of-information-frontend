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
import forms.ContactPhoneFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ContactDetailsNavigator
import pages.{ContactNamePage, ContactPhonePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactPhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: ContactPhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def data(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): JsObject = {

    val name = request.userAnswers.get(ContactNamePage)
    Json.obj(
      "form"      -> form,
      "name"      -> name,
      "pageTitle" -> "contactPhone.title.business",
      "heading"   -> "contactPhone.heading.business",
      "hintText"  -> hintWithNoBreakSpaces(),
      "action"    -> routes.ContactPhoneController.onSubmit(mode).url
    )
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        val formWithData = request.userAnswers.get(ContactPhonePage).fold(form)(form.fill)
        renderer.render("contactPhone.njk", data(mode, formWithData)).map(Ok(_))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => renderer.render("contactPhone.njk", data(mode, formWithErrors)).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactPhonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContactPhonePage, mode, updatedAnswers))
          )
    }

  private def hintWithNoBreakSpaces()(implicit messages: Messages): Html =
    Html(
      s"${messages("contactPhone.hint")}"
    )
}
