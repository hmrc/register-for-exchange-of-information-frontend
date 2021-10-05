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
import forms.AddressWithoutIdFormProvider
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.requests.DataRequest
import models.{Address, Country, Mode}
import navigation.MDRNavigator
import pages.{AddressWithoutIdPage, WhatAreYouRegisteringAsPage}
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

class AddressWithoutIdController @Inject() (
  override val messagesApi: MessagesApi,
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataInitializeAction, // TODO replace with DataRequiredAction when actual flow is ready
  formProvider: AddressWithoutIdFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  //val countries: Seq[Country] = countryListFactory.getCountryList.getOrElse(throw new Exception("Cannot retrieve country list"))

  //private val form = formProvider(countries)

  private def getFormWithCountries(registeringAsBusiness: Boolean): Seq[Country] = {
    val countries: Seq[Country] = countryListFactory.getCountryList.getOrElse(throw new Exception("Cannot retrieve country list"))
    if (registeringAsBusiness) countries else countries.filter(_.code != "GB")
  }

  private def render(mode: Mode, form: Form[Address], registeringAsBusiness: Boolean, countries: Seq[Country])(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val data = Json.obj(
      "form"                  -> form,
      "action"                -> routes.AddressWithoutIdController.onSubmit(mode).url,
      "registeringAsBusiness" -> registeringAsBusiness,
      "countries"             -> countryJsonList(form.data, countries)
    )
    renderer.render("addressWithoutId.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val registeringAsBusiness: Boolean =
        request.userAnswers.get(WhatAreYouRegisteringAsPage) match { //ToDo defaulting to registering for business change when paths created if necessary
          case Some(RegistrationTypeBusiness) => true
          case _                              => false
        }
      val countries = getFormWithCountries(registeringAsBusiness)
      val form      = formProvider(countries)

      render(mode, request.userAnswers.get(AddressWithoutIdPage).fold(form)(form.fill), registeringAsBusiness, countries).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val registeringAsBusiness: Boolean =
        request.userAnswers.get(WhatAreYouRegisteringAsPage) match { //ToDo defaulting to registering for business change when paths created if necessary
          case Some(RegistrationTypeBusiness) => true
          case _                              => false
        }
      val countries = getFormWithCountries(registeringAsBusiness)

      formProvider(countries)
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, formWithErrors, registeringAsBusiness, countries).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressWithoutIdPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddressWithoutIdPage, mode, updatedAnswers))
        )
  }

  private def countryJsonList(value: Map[String, String], countries: Seq[Country]): Seq[JsObject] = {
    def containsCountry(country: Country): Boolean =
      value.get("country") match {
        case Some(countrycode) => countrycode == country.code
        case _                 => false
      }

    val countryJsonList = countries.map {
      country =>
        Json.obj("text" -> country.description, "value" -> country.code, "selected" -> containsCountry(country))
    }

    Json.obj("value" -> "", "text" -> "") +: countryJsonList
  }
}
