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

import config.FrontendAppConfig
import controllers.actions._
import forms.IsThisYourBusinessFormProvider
import models.matching.{OrgRegistrationInfo, RegistrationRequest}
import models.register.request.RegisterWithID
import models.register.response.details.AddressResponse
import models.requests.DataRequest
import models.{BusinessType, Mode, NormalMode, Regime}
import navigation.MDRNavigator
import pages._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import services.{BusinessMatchingWithIdService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: IsThisYourBusinessFormProvider,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  private val form = formProvider()

  private def result(mode: Mode, regime: Regime, form: Form[Boolean], registrationInfo: OrgRegistrationInfo)(implicit
    ec: ExecutionContext,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.getDisplaySubscriptionId(regime, registrationInfo.safeId) flatMap {
      case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(registrationInfo.safeId, subscriptionId, regime)
      case _ =>
        val name     = registrationInfo.name
        val address  = registrationInfo.address
        val withForm = request.userAnswers.get(IsThisYourBusinessPage).fold(form)(form.fill)
        render(mode, regime, withForm, name, address).map(Ok(_))
    }

  private def render(mode: Mode, regime: Regime, form: Form[Boolean], name: String, address: AddressResponse)(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val data = Json.obj(
      "form"    -> form,
      "regime"  -> regime.toUpperCase,
      "name"    -> name,
      "address" -> address.asList,
      "action"  -> routes.IsThisYourBusinessController.onSubmit(mode, regime).url,
      "radios"  -> Radios.yesNo(form("value"))
    )
    renderer.render("isThisYourBusiness.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      buildRegisterWithId(regime) match {
        case Some(registerWithID) =>
          matchingService.sendBusinessRegistrationInformation(registerWithID).flatMap {
            case Right(response) =>
              request.userAnswers.set(RegistrationInfoPage, response).map(sessionRepository.set)
              result(mode, regime, form, response)
            case _ =>
              Future.successful(Redirect(routes.BusinessNotIdentifiedController.onPageLoad(regime)))
          }
        case _ =>
          renderer
            .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
            .map(InternalServerError(_))
      }
  }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] = standardActionSets.identifiedUserWithData(regime).async {
    implicit request =>
      val thereIsAProblem =
        renderer.render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries)).map(ServiceUnavailable(_))
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.get(RegistrationInfoPage).fold(thereIsAProblem) {
              case OrgRegistrationInfo(_, name, address) =>
                render(mode, regime, formWithErrors, name, address).map(BadRequest(_))
              case _ => thereIsAProblem
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, NormalMode, regime, updatedAnswers))
        )
  }

  def buildRegisterWithId(regime: Regime)(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    request.userAnswers.get(BusinessTypePage) flatMap {
      case BusinessType.Sole => buildIndividualRegistrationRequest(regime)
      case _                 => buildBusinessRegistrationRequest(regime)
    }

  def buildBusinessRegistrationRequest(regime: Regime)(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr          <- request.userAnswers.get(UTRPage)
      businessName <- request.userAnswers.get(BusinessNamePage)
      businessType = request.userAnswers.get(BusinessTypePage)
    } yield RegisterWithID(regime, RegistrationRequest("UTR", utr.uniqueTaxPayerReference, businessName, businessType, None))

  def buildIndividualRegistrationRequest(regime: Regime)(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr             <- request.userAnswers.get(UTRPage)
      soleTradersName <- request.userAnswers.get(SoleNamePage)
    } yield RegisterWithID(regime, soleTradersName, None, "UTR", utr.uniqueTaxPayerReference)

}
