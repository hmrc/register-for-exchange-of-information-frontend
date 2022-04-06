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
import forms.AddressUKFormProvider
import models.requests.DataRequest
import models.{Address, Country, Mode, Regime}
import navigation.MDRNavigator
import pages.AddressUKPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
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
  standardActionSets: StandardActionSets,
  formProvider: AddressUKFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  val countriesList: Option[Seq[Country]] = countryListFactory.countryList

  private def render(mode: Mode, regime: Regime, form: Form[Address])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"      -> form,
      "regime"    -> regime.toUpperCase,
      "action"    -> routes.AddressUKController.onSubmit(mode, regime).url,
      "countries" -> Seq(Json.obj("text" -> "United Kingdom", "value" -> "GB", "selected" -> true))
    )
    renderer.render("addressUK.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        countriesList match {
          case Some(countries) =>
            val form = formProvider(countries)
            render(mode, regime, request.userAnswers.get(AddressUKPage).fold(form)(form.fill)).map(Ok(_))
          case None =>
            logger.error("Could not retrieve countries list from JSON file.")
            Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad(regime)))
        }
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(regime).async {
      implicit request =>
        countriesList match {
          case Some(countries) =>
            formProvider(countries)
              .bindFromRequest()
              .fold(
                formWithErrors => render(mode, regime, formWithErrors).map(BadRequest(_)),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(AddressUKPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(AddressUKPage, mode, regime, updatedAnswers))
              )
          case None =>
            logger.error("Could not retrieve countries list from JSON file.")
            Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad(regime)))
        }
    }
}
