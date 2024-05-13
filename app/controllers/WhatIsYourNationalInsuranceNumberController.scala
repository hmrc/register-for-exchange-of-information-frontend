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
import forms.WhatIsYourNationalInsuranceNumberFormProvider
import models.Mode
import navigation.MDRNavigator
import pages.WhatIsYourNationalInsuranceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatIsYourNationalInsuranceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourNationalInsuranceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: WhatIsYourNationalInsuranceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsYourNationalInsuranceNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() { implicit request =>
    val preparedForm = request.userAnswers.get(WhatIsYourNationalInsuranceNumberPage) match {
      case None       => form
      case Some(nino) => form.fill(nino.nino)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <-
              Future.fromTry(request.userAnswers.set(WhatIsYourNationalInsuranceNumberPage, Nino(value)))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatIsYourNationalInsuranceNumberPage, mode, updatedAnswers))
      )
  }
}
