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
import forms.SecondContactFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.CBCRNavigator
import pages.SecondContactPage
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

class SecondContactController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: CBCRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SecondContactFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[Boolean], name: String)(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "action" -> routes.SecondContactController.onSubmit(mode, regime).url,
      "name"   -> name,
      "radios" -> Radios.yesNo(form("value"))
    )
    renderer.render("secondContact.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        SomeInformationIsMissing.isMissingContactName(regime) {
          render(mode, regime, request.userAnswers.get(SecondContactPage).fold(form)(form.fill), _).map(Ok(_))
        }
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              SomeInformationIsMissing.isMissingContactName(regime) {
                render(mode, regime, request.userAnswers.get(SecondContactPage).fold(formWithErrors)(formWithErrors.fill), _).map(BadRequest(_))
              },
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SecondContactPage, mode, regime, updatedAnswers))
          )
    }
}
