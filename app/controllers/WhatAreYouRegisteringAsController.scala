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
import forms.WhatAreYouRegisteringAsFormProvider
import models.requests.DataRequest
import models.{Mode, WhatAreYouRegisteringAs}
import navigation.MDRNavigator
import pages.WhatAreYouRegisteringAsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatAreYouRegisteringAsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: WhatAreYouRegisteringAsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[WhatAreYouRegisteringAs])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "action" -> routes.WhatAreYouRegisteringAsController.onSubmit(mode).url,
      "radios" -> WhatAreYouRegisteringAs.radios(form)
    )
    renderer.render("whatAreYouRegisteringAs.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        render(mode, request.userAnswers.get(WhatAreYouRegisteringAsPage).fold(form)(form.fill)).map(Ok(_))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatAreYouRegisteringAsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(WhatAreYouRegisteringAsPage, mode, updatedAnswers))
          )
    }
}
