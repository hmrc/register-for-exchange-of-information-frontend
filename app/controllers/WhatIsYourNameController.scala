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
import forms.WhatIsYourNameFormProvider
import models.requests.DataRequest
import models.{Mode, Name, Regime}
import navigation.MDRNavigator
import pages.WhatIsYourNamePage
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

class WhatIsYourNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatIsYourNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with WithEitherT {

  private val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[Name])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "regime" -> regime.toUpperCase,
      "action" -> routes.WhatIsYourNameController.onSubmit(mode, regime).url
    )
    renderer.render("whatIsYourName.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        render(mode, regime, request.userAnswers.get(WhatIsYourNamePage).fold(form)(form.fill)).map(Ok(_))
    }

  /*
  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
            value => {

              val originalAnswer = request.userAnswers.get(WhatIsYourNamePage)

              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourNamePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPageWithValueCheck(WhatIsYourNamePage, mode, regime, updatedAnswers, originalAnswer))
            }
          )
    }
   */

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
            value =>
              (for {
                updatedAnswers <- setEither(WhatIsYourNamePage, value, checkPrevious = true)
                _ = sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(WhatIsYourNamePage, mode, regime, updatedAnswers)))
                .valueOrF(
                  _ => renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
                )
          )
    }
}
