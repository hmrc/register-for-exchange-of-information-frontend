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
import forms.IndividualHaveContactTelephoneFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.ContactDetailsNavigator
import pages.IndividualHaveContactTelephonePage
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

class IndividualHaveContactTelephoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: IndividualHaveContactTelephoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[Boolean])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "regime" -> regime.toUpperCase,
      "action" -> routes.IndividualHaveContactTelephoneController.onSubmit(mode, regime).url,
      "radios" -> Radios.yesNo(form("value"))
    )
    renderer.render("individualHaveContactTelephone.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        render(mode, regime, request.userAnswers.get(IndividualHaveContactTelephonePage).fold(form)(form.fill)).map(Ok(_))
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualHaveContactTelephonePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IndividualHaveContactTelephonePage, mode, regime, updatedAnswers))
          )
    }
}