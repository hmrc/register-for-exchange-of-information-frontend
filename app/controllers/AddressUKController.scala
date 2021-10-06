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

import controllers.actions._
import forms.AddressUKFormProvider
import models.requests.DataRequest
import models.{Address, Country, Mode}
import navigation.MDRNavigator
import pages.AddressUKPage
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CountryListFactory

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressUKController @Inject() (
  override val messagesApi: MessagesApi,
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataInitializeAction,
  formProvider: AddressUKFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {
  private val logger: Logger = Logger(this.getClass)

  val countriesList: Option[Seq[Country]] = countryListFactory.getCountryList

  private def render(mode: Mode, form: Form[Address])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"      -> form,
      "action"    -> routes.AddressUKController.onSubmit(mode).url,
      "countries" -> countryJsonList
    )
    renderer.render("addressUK.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      countriesList match {
        case Some(countries) =>
          val form = formProvider(countries)
          render(mode, request.userAnswers.get(AddressUKPage).fold(form)(form.fill)).map(Ok(_))
        case None =>
          logger.error("Could not retrieve countries list from JSON file.")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      countriesList match {
        case Some(countries) =>
          formProvider(countries)
            .bindFromRequest()
            .fold(
              formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressUKPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddressUKPage, mode, updatedAnswers))
            )
        case None =>
          logger.error("Could not retrieve countries list from JSON file.")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  private def countryJsonList: Seq[JsObject] = Seq(
    Json.obj("text" -> "United Kingdom", "value" -> "GB", "selected" -> true)
  )
}
