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
import forms.UTRFormProvider
import models.BusinessType._
import models.requests.DataRequest
import models.{BusinessType, Mode, Regime, UniqueTaxpayerReference}
import navigation.MDRNavigator
import pages.{BusinessTypePage, UTRPage}
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

class UTRController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: UTRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val ct = "utr.error.ct"
  private val sa = "utr.error.sa"

  private def readKey(key: String)(implicit messages: Messages) = messages(key)

  private def render(mode: Mode, regime: Regime, form: Form[UniqueTaxpayerReference], businessType: BusinessType)(implicit
    request: DataRequest[AnyContent]
  ): Future[api.Html] = {
    val taxType = businessType match {
      case Partnership | Sole | LimitedPartnership => readKey(sa)
      case _                                       => readKey(ct)
    }

    val data = Json.obj(
      "form"       -> form,
      "regime"     -> regime.toUpperCase,
      "taxType"    -> taxType,
      "lostUTRUrl" -> appConfig.lostUTRUrl,
      "action"     -> routes.UTRController.onSubmit(mode, regime).url,
      "hintText"   -> hintWithLostUtrLink(taxType)
    )
    renderer.render("utr.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithRequiredAnswer(RequiredAnswer(BusinessTypePage, regime)).async {
      implicit request =>
        val businessType = request.userAnswers.get(BusinessTypePage).get

        val form = formProvider(businessType match {
          case Partnership | Sole | LimitedPartnership => readKey(sa)
          case _                                       => readKey(ct)
        })
        render(mode, regime, request.userAnswers.get(UTRPage).fold(form)(form.fill), businessType).map(Ok(_))
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithRequiredAnswer(RequiredAnswer(BusinessTypePage, regime)).async {
      implicit request =>
        val businessType = request.userAnswers.get(BusinessTypePage).get

        formProvider(businessType match {
          case Partnership | Sole | LimitedPartnership => readKey(sa)
          case _                                       => readKey(ct)
        })
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors, businessType).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.setOrCleanup(UTRPage, value, true))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(UTRPage, mode, regime, updatedAnswers))
          )
    }

  private def hintWithLostUtrLink(taxType: String)(implicit messages: Messages): Html =
    Html(
      s"${messages("utr.hint", taxType)} <span> <a class='govuk-link text-overflow' href='${appConfig.lostUTRUrl}' rel='noreferrer noopener' target='_blank'>" +
        s"${messages("utr.hint.link")}</a>.</span>"
    )
}
