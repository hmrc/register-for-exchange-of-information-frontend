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
import exceptions.SomeInformationIsMissingException
import forms.BusinessNameFormProvider
import models.BusinessType._

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
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

import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: BusinessNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  val llpHeading = "registered name of your business"
  val llpHint    = "This is the registered nam of your incorporated certificate."

  val partnershipHeading = "partnership name"
  val partnershipHint    = "This is the name that you registered with HMRC."

  val unincorporatedHeading = "name of your organisation"
  val unincorporatedHint    = "This is the name on your governing document."

  private def pageHeadingAndHint()(implicit request: DataRequest[AnyContent]): (String, String) =
    request.userAnswers.get(BusinessTypePage).getOrElse(throw new SomeInformationIsMissingException("Missing business type")) match {
      case LimitedPartnership | LimitedCompany => (llpHeading, llpHint)
      case Partnership                         => (partnershipHint, partnershipHint)
      case UnincorporatedAssociation           => (unincorporatedHeading, unincorporatedHint)
    }

  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val (heading, hint) = pageHeadingAndHint

    val data = Json.obj(
      "form"    -> form,
      "heading" -> heading,
      "hinr"    -> hint,
      "action"  -> routes.BusinessNameController.onSubmit(mode).url
    )
    renderer.render("businessName.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      render(mode, request.userAnswers.get(BusinessNamePage).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(BusinessNamePage, mode, updatedAnswers))
        )
  }
}
