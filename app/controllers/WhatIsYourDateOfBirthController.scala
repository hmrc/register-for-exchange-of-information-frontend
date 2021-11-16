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

import cats.implicits._
import controllers.actions._
import forms.WhatIsYourDateOfBirthFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.MDRNavigator
import pages.WhatIsYourDateOfBirthPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}
import utils.UserAnswersHelper

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WhatIsYourDateOfBirthController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatIsYourDateOfBirthFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with WithEitherT {

  val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[LocalDate])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "regime" -> regime.toUpperCase,
      "action" -> routes.WhatIsYourDateOfBirthController.onSubmit(mode, regime).url,
      "date"   -> DateInput.localDate(form("value"))
    )
    renderer.render("whatIsYourDateOfBirth.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        render(mode, regime, request.userAnswers.get(WhatIsYourDateOfBirthPage).fold(form)(form.fill)).map(Ok(_))
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
            value =>
              (for {
                updatedAnswers <- setEither(WhatIsYourDateOfBirthPage, value, checkPrevious = true)
                _ = sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(WhatIsYourDateOfBirthPage, mode, regime, updatedAnswers)))
                .valueOrF(
                  _ => renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
                )
          )
    }
}
