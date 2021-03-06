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

import config.FrontendAppConfig
import controllers.actions._
import forms.DoYouHaveUniqueTaxPayerReferenceFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.MDRNavigator
import pages.DoYouHaveUniqueTaxPayerReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoYouHaveUniqueTaxPayerReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: DoYouHaveUniqueTaxPayerReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[Boolean])(implicit request: DataRequest[AnyContent]): Future[api.Html] = {
    val data = Json.obj(
      "form"     -> form,
      "action"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onSubmit(mode).url,
      "radios"   -> Radios.yesNo(form("value")),
      "hintText" -> hintWithLostUtrLink
    )
    renderer.render("doYouHaveUniqueTaxPayerReference.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithInitializedData().async {
      implicit request =>
        render(mode, request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage).fold(form)(form.fill)).map(Ok(_))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithInitializedData().async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouHaveUniqueTaxPayerReferencePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(DoYouHaveUniqueTaxPayerReferencePage, mode, updatedAnswers))
          )
    }

  private def hintWithLostUtrLink()(implicit messages: Messages): Html =
    Html(
      s"${messages("doYouHaveUniqueTaxPayerReference.hint")}<span> <a class='govuk-link text-overflow' href='${appConfig.lostUTRUrl}' rel='noreferrer noopener' target='_blank'>" +
        s"${messages("doYouHaveUniqueTaxPayerReference.hint.link")}</a>.</span>"
    )
}
