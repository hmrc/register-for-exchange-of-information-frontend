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

import controllers.actions.StandardActionSets
import forms.BusinessNameFormProvider
import models.BusinessType._
import models.{BusinessType, Mode}
import navigation.MDRNavigator
import pages.{BusinessNamePage, BusinessTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: BusinessNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def selectedBusinessTypeText(businessType: BusinessType): Option[String] =
    businessType match {
      case LimitedPartnership | LimitedCompany => Some("llp")
      case Partnership                         => Some("partner")
      case UnincorporatedAssociation           => Some("unincorporated")
      case _                                   => None
    }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData() {
      implicit request =>
        selectedBusinessTypeText(request.userAnswers.get(BusinessTypePage).get) match {
          case Some(businessTypeText) =>
            val form = formProvider(businessTypeText)
            val preparedForm = selectedBusinessTypeText(request.userAnswers.get(BusinessTypePage).get) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, mode))
          case _ => Redirect(routes.ThereIsAProblemController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        selectedBusinessTypeText(request.userAnswers.get(BusinessTypePage).get) match {
          case Some(businessTypeText) =>
            formProvider(businessTypeText)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(BusinessNamePage, mode, updatedAnswers))
              )
          case _ => Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
        }

    }

}
