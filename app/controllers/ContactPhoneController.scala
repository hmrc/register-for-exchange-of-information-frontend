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
import forms.ContactPhoneFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.CBCRNavigator
import pages.{ContactNamePage, ContactPhonePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.UserAnswersHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactPhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: CBCRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ContactPhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with UserAnswersHelper {

  private val form = formProvider()

  private val businessTitleKey     = "contactPhone.title"
  private val businessHeadingKey   = "contactPhone.heading"
  private val individualTitleKey   = "contactPhone.individual.heading"
  private val individualHeadingKey = "contactPhone.individual.heading"

  private def render(mode: Mode, regime: Regime, form: Form[String], name: String = "")(implicit request: DataRequest[AnyContent]): Future[Html] = {

    val (pageTitle, heading) = if (hasContactName()) {
      (businessTitleKey, businessHeadingKey)
    } else {
      (individualTitleKey, individualHeadingKey)
    }

    val data = Json.obj(
      "form"      -> form,
      "regime"    -> regime.toUpperCase,
      "name"      -> name,
      "pageTitle" -> pageTitle,
      "heading"   -> heading,
      "action"    -> routes.ContactPhoneController.onSubmit(mode, regime).url
    )
    renderer.render("contactPhone.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        request.userAnswers
          .get(ContactNamePage) match {
          case Some(contactName) => render(mode, regime, request.userAnswers.get(ContactPhonePage).fold(form)(form.fill), contactName).map(Ok(_))
          case _                 => render(mode, regime, request.userAnswers.get(ContactPhonePage).fold(form)(form.fill)).map(Ok(_))
        }

    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, request.userAnswers.get(ContactPhonePage).fold(formWithErrors)(formWithErrors.fill)).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactPhonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContactPhonePage, mode, regime, updatedAnswers))
          )
    }
}
