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

import config.FrontendAppConfig
import controllers.actions._
import forms.IsContactTelephoneFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.ContactDetailsNavigator
import pages.{ContactNamePage, IsContactTelephonePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
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
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IsContactTelephoneFormProvider,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with UserAnswersHelper {

  def form(suffix: String) = formProvider(suffix)

  private def data(mode: Mode, regime: Regime, form: Form[Boolean], suffix: String)(implicit request: DataRequest[AnyContent]): Option[JsObject] = {
    val orIndividual = if (suffix == "individual") Some("") else None
    request.userAnswers.get(ContactNamePage).orElse(orIndividual).map {
      name =>
        val filledForm = request.userAnswers.get(IsContactTelephonePage).fold(form)(form.fill)
        Json.obj(
          "form"      -> filledForm,
          "regime"    -> regime.toUpperCase,
          "name"      -> name,
          "pageTitle" -> s"isContactTelephone.title.$suffix",
          "heading"   -> s"isContactTelephone.heading.$suffix",
          "action"    -> routes.IsContactTelephoneController.onSubmit(mode, regime).url,
          "radios"    -> Radios.yesNo(filledForm("value"))
        )
    }
  }

  private def thereIsAProblem(regime: Regime)(implicit request: DataRequest[AnyContent]): Future[Result] =
    renderer
      .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
      .map(BadRequest(_))

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        val suffix = isBusinessOrIndividual()
        data(mode, regime, form(suffix), suffix).fold(thereIsAProblem(regime)) {
          data =>
            renderer.render("isContactTelephone.njk", data).map(Ok(_))
        }
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        val suffix = isBusinessOrIndividual()
        form(suffix)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              data(mode, regime, formWithErrors, suffix).fold(thereIsAProblem(regime)) {
                data =>
                  renderer.render("isContactTelephone.njk", data).map(BadRequest(_))
              },
            value => {
              val originalAnswer = request.userAnswers.get(IsContactTelephonePage)
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsContactTelephonePage, value, originalAnswer))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsContactTelephonePage, mode, regime, updatedAnswers))
            }
          )
    }
}
