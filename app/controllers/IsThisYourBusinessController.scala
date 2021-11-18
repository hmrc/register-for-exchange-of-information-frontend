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

import cats.implicits.catsStdInstancesForFuture
import controllers.actions._
import forms.IsThisYourBusinessFormProvider
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.MDRNavigator
import pages._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IsThisYourBusinessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with WithEitherT {

  private val form = formProvider()

  private def render(mode: Mode, regime: Regime, form: Form[Boolean])(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    for {
      registrationInfo <- getEither(RegistrationInfoPage)
      name             <- getEither(registrationInfo.name, "Missing registration name.")
      address          <- getEither(registrationInfo.address, "Missing registration address.")

      _ = println(s"\nregistrationInfo = $registrationInfo\nname = $name\nadress = $address") // todo del me

      withForm <- getEither(IsThisYourBusinessPage)
        .map(form.fill)
        .recover {
          case _ => form
        }
    } yield Json.obj(
      "form"    -> withForm,
      "regime"  -> regime.toUpperCase,
      "name"    -> name,
      "address" -> address.asList,
      "action"  -> routes.IsThisYourBusinessController.onSubmit(mode, regime).url,
      "radios"  -> Radios.yesNo(withForm("value"))
    )
  }.semiflatMap {
    s => renderer.render("isThisYourBusiness.njk", s).map(Ok(_))
  }.valueOrF(
    _ => renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
  )

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request => render(mode, regime, form)
  }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, regime, formWithErrors),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, regime, updatedAnswers))
        )
  }
}
