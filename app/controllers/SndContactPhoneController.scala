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
import exceptions.SomeInformationIsMissingException
import forms.SndContactPhoneFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ContactDetailsNavigator
import pages.{SndContactNamePage, SndContactPhonePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SndContactPhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: SndContactPhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[String], name: String)(implicit request: DataRequest[AnyContent]): Future[api.Html] = {
    val data = Json.obj(
      "form"     -> form,
      "name"     -> request.userAnswers.get(SndContactNamePage).getOrElse(throw new SomeInformationIsMissingException("Missing contact name")),
      "hintText" -> hintWithNoBreakSpaces(),
      "action"   -> routes.SndContactPhoneController.onSubmit(mode).url
    )
    renderer.render("sndContactPhone.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(SndContactNamePage).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(SndContactPhonePage) match {
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
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SndContactPhonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SndContactPhonePage, mode, updatedAnswers))
          )
    }

  private def hintWithNoBreakSpaces()(implicit messages: Messages): Html =
    Html(
      s"${messages("sndContactPhone.hint")}"
    )
}
