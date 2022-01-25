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
import forms.BusinessNameFormProvider
import models.BusinessType._
import models.requests.DataRequest
import models.{BusinessType, Mode, Regime}
import navigation.MDRNavigator
import pages.{BusinessNamePage, BusinessTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: BusinessNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private def selectedBusinessTypeText(businessType: BusinessType): String =
    businessType match {
      case LimitedPartnership | LimitedCompany => "llp"
      case Partnership                         => "partner"
      case UnincorporatedAssociation           => "unincorporated"
    }

  private def render(mode: Mode, regime: Regime, form: Form[String], businessType: String)(implicit request: DataRequest[AnyContent]): Future[Html] = {

    val data = Json.obj(
      "form"     -> form,
      "regime"   -> regime.toUpperCase,
      "titleTxt" -> s"businessName.title.$businessType",
      "heading"  -> s"businessName.heading.$businessType",
      "hint"     -> s"businessName.hint.$businessType",
      "action"   -> routes.BusinessNameController.onSubmit(mode, regime).url
    )
    renderer.render("businessName.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(BusinessTypePage, regime).async {
      implicit request =>
        val businessTypeText = selectedBusinessTypeText(request.userAnswers.get(BusinessTypePage).get)

        val form = formProvider(businessTypeText)
        render(mode, regime, request.userAnswers.get(BusinessNamePage).fold(form)(form.fill), businessTypeText).map(Ok(_))

    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(BusinessTypePage, regime).async {
      implicit request =>
        val businessTypeText = selectedBusinessTypeText(request.userAnswers.get(BusinessTypePage).get)

        formProvider(businessTypeText)
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors, businessTypeText).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.setOrCleanup(BusinessNamePage, value, true))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(BusinessNamePage, mode, regime, updatedAnswers))
          )
    }
}
