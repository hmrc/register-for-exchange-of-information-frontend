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
import forms.BusinessWithoutIDNameFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigation.{MDRNavigator, Navigator}
import pages.BusinessWithoutIDNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class BusinessWithoutIDNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: BusinessWithoutIDNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "action" -> routes.BusinessWithoutIDNameController.onSubmit(mode).url
    )
    renderer.render("businessWithoutIDName.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      render(mode, request.userAnswers.get(BusinessWithoutIDNamePage).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessWithoutIDNamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(BusinessWithoutIDNamePage, mode, updatedAnswers))
        )
  }
}