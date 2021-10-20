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
import models.{Address, Country, Mode, Regime}
import navigation.MDRNavigator
import pages.{AddressWithoutIdPage, WhatAreYouRegisteringAsPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, MessagesControllerComponents}
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
  private val logger: Logger = Logger(this.getClass)

  val countriesList: Option[Seq[Country]] = countryListFactory.countryList

  private def render(mode: Mode, regime: Regime, form: Form[Address], registeringAsBusiness: Boolean, countries: Seq[Country])(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val data = Json.obj(
      "form"                  -> form,
      "action"                -> routes.AddressWithoutIdController.onSubmit(mode, regime).url,
      "registeringAsBusiness" -> registeringAsBusiness,
      "countries"             -> countryJsonList(form.data, countries)
    )
    renderer.render("addressWithoutId.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        val registeringAsBusiness = getRegisteringAsBusiness()

        countriesList match {
          case Some(countries) =>
            val filteredCountries = if (registeringAsBusiness) countries else countries.filter(_.code != "GB")
            val form              = formProvider(filteredCountries)
            render(mode, regime, request.userAnswers.get(AddressWithoutIdPage).fold(form)(form.fill), registeringAsBusiness, filteredCountries).map(Ok(_))
          case None =>
            logger.error("Could not retrieve countries list from JSON file.")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad(regime)))
        }
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify andThen getData.apply andThen requireData).async {
      implicit request =>
        val registeringAsBusiness = getRegisteringAsBusiness()

        countriesList match {
          case Some(countries) =>
            val filteredCountries = if (registeringAsBusiness) countries else countries.filter(_.code != "GB")
            formProvider(filteredCountries)
              .bindFromRequest()
              .fold(
                formWithErrors => render(mode, regime, formWithErrors, registeringAsBusiness, filteredCountries).map(BadRequest(_)),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressWithoutIdPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(AddressWithoutIdPage, mode, regime, updatedAnswers))
              )
          case None =>
            logger.error("Could not retrieve countries list from JSON file.")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad(regime)))
        }
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

  private def getRegisteringAsBusiness()(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.get(WhatAreYouRegisteringAsPage) match { //ToDo defaulting to registering for business change when paths created if necessary
      case Some(RegistrationTypeBusiness) => true
      case _                              => false
    }
}
