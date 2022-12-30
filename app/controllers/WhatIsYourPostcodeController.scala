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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.WhatIsYourPostcodeFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.MDRNavigator
import pages.{AddressLookupPage, WhatIsYourDateOfBirthPage, WhatIsYourPostcodePage}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import views.html.WhatIsYourPostCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourPostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: WhatIsYourPostcodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  addressLookupConnector: AddressLookupConnector,
  view: WhatIsYourPostCodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

//  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
//    val data = Json.obj(
//      "form"             -> form,
//      "manualAddressUrl" -> routes.AddressUKController.onPageLoad(mode).url,
//      "action"           -> routes.WhatIsYourPostcodeController.onSubmit(mode).url
//    )
//    renderer.render("whatIsYourPostcode.njk", data)
//  }

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatIsYourPostcodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithData().async {
      implicit request =>
        val formReturned = form.bindFromRequest()

        formReturned
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            postCode =>
              addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
                case Nil =>
                  val formError = formReturned.withError(FormError("postCode", List("whatIsYourPostcode.error.notFound")))
                  Future.successful(BadRequest(view(formError, mode)))

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
