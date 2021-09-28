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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.WhatIsYourPostcodeFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.MDRNavigator
import pages.{AddressLookupPage, WhatIsYourPostcodePage}
import play.api.data.{Form, FormError}
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

class WhatIsYourPostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatIsYourPostcodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  addressLookupConnector: AddressLookupConnector,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"             -> form,
      "action"           -> routes.WhatIsYourPostcodeController.onSubmit(mode).url,
      "manualAddressUrl" -> routes.AddressUKController.onPageLoad(mode).url
    )
    renderer.render("whatIsYourPostcode.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      render(mode, request.userAnswers.get(WhatIsYourPostcodePage).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val formReturned = form.bindFromRequest()

      formReturned
        .fold(
          formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
          postCode =>
            addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
              case Nil =>
                val formError = formReturned.withError(FormError("postCode", List("individualUKPostcode.error.notFound")))
                render(mode, formError).map(BadRequest(_))

              case addresses =>
                for {
                  updatedAnswers            <- Future.fromTry(request.userAnswers.set(WhatIsYourPostcodePage, postCode))
                  updatedAnswersWithAddress <- Future.fromTry(updatedAnswers.set(AddressLookupPage, addresses))
                  _                         <- sessionRepository.set(updatedAnswersWithAddress)
                } yield Redirect(navigator.nextPage(WhatIsYourPostcodePage, mode, updatedAnswersWithAddress))
            }
        )
  }
}
