/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.SndConHavePhoneFormProvider
import models.Mode
import navigation.ContactDetailsNavigator
import pages.SndConHavePhonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.SndConHavePhoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SndConHavePhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  standardActionSets: StandardActionSets,
  formProvider: SndConHavePhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SndConHavePhoneView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val preparedForm = request.userAnswers.get(SndConHavePhonePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, getSecondContactName(request.userAnswers), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, getSecondContactName(request.userAnswers), mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SndConHavePhonePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SndConHavePhonePage, mode, updatedAnswers))
        )
  }
}
