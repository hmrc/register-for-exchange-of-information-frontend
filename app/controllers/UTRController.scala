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

import config.FrontendAppConfig
import controllers.actions._
import forms.UTRFormProvider
import models.ReporterType._
import models.{Mode, UserAnswers}
import navigation.MDRNavigator
import pages.{ReporterTypePage, UTRPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UTRView

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
  view: UTRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(ReporterTypePage).async { implicit request =>
      val taxType = getTaxType(request.userAnswers)
      val form    = formProvider(taxType)

      val preparedForm = request.userAnswers.get(UTRPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Future.successful(Ok(view(preparedForm, mode, taxType)))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(ReporterTypePage).async { implicit request =>
      val taxType = getTaxType(request.userAnswers)
      val form    = formProvider(taxType)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, taxType))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UTRPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UTRPage, mode, updatedAnswers))
        )
    }

  private def getTaxType(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers.get(ReporterTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => messages("utr.error.corporationTax")
      case _                                                      => messages("utr.error.selfAssessment")
    }

}
