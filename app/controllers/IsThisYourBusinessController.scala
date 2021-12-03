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

import cats.data.EitherT
import cats.implicits._
import config.FrontendAppConfig
import controllers.actions._
import forms.IsThisYourBusinessFormProvider
import models.error.ApiError
import models.error.ApiError.NotFoundError
import models.matching.RegistrationInfo
import models.register.response.details.AddressResponse
import models.requests.DataRequest
import models.{Mode, Regime}
import navigation.MDRNavigator
import pages._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import services.{BusinessMatchingService, SubscriptionService}
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
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with WithEitherT
    with Logging {

  private val form = formProvider()

  private def result(mode: Mode, regime: Regime, form: Form[Boolean])(implicit ec: ExecutionContext, request: DataRequest[AnyContent]) =
    (for {
      registrationInfo <- EitherT(matchBusinessInfo(regime))
      updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
      _ = sessionRepository.set(updatedAnswers)
    } yield registrationInfo)
      .fold(
        fa = {
          case NotFoundError =>
            Future.successful(Redirect(routes.NoRecordsMatchedController.onPageLoad(regime)))
          case _ =>
            renderer
              .render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
              .map(ServiceUnavailable(_))
        },
        fb =>
          subscriptionService.getDisplaySubscriptionId(regime, fb.safeId) flatMap {
            case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(fb.safeId, subscriptionId, regime)
            case _ =>
              val name     = fb.name.getOrElse("")
              val address  = fb.address.getOrElse(AddressResponse("", None, None, None, None, ""))
              val withForm = request.userAnswers.get(IsThisYourBusinessPage).fold(form)(form.fill)
              render(mode, regime, withForm, name, address).map(Ok(_))
          }
      )
      .flatten

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

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request => result(mode, regime, form)
  }

  private def matchBusinessInfo(regime: Regime)(implicit request: DataRequest[AnyContent]): Future[Either[ApiError, RegistrationInfo]] =
    (for {
      utr          <- getEither(UTRPage)
      businessName <- getEither(BusinessNamePage).orElse(getEither(SoleNamePage).map(_.fullName))
      businessType <- getEither(BusinessTypePage)
      dob = request.userAnswers.get(SoleDateOfBirthPage)
      registrationInfo <- EitherT(matchingService.sendBusinessRegistrationInformation(regime, RegistrationInfo.build(businessType, businessName, utr, dob)))
    } yield registrationInfo).value

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => result(mode, regime, formWithErrors),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, regime, updatedAnswers))
        )
  }
}
