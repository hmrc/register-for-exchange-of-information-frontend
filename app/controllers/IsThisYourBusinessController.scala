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
import forms.IsThisYourBusinessFormProvider
import models.{Mode, Regime}
import models.matching.MatchingInfo
import models.register.error.ApiError
import models.register.error.ApiError.{MandatoryInformationMissingError, NotFoundError}
import models.register.response.details.AddressResponse
import models.requests.DataRequest
import navigation.{MDRNavigator, Navigator}
import pages._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import services.BusinessMatchingService
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
  matchingService: BusinessMatchingService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def result(mode: Mode, regime: Regime, form: Form[Boolean])(implicit request: DataRequest[AnyContent]) =
    matchBusinessInfo flatMap {
      case Right(matchingInfo) =>
        (for {
          name           <- matchingInfo.name
          address        <- matchingInfo.address
          updatedAnswers <- request.userAnswers.set(MatchingInfoPage, matchingInfo).toOption
          _ = sessionRepository.set(updatedAnswers)
        } yield render(mode, regime, request.userAnswers.get(IsThisYourBusinessPage).fold(form)(form.fill), name, address).map(Ok(_)))
          .getOrElse(Future.successful(Redirect(Navigator.missingInformation(regime))))
      case Left(NotFoundError) =>
        Future.successful(Redirect(routes.NoRecordsMatchedController.onPageLoad(regime)))
      case _ =>
        renderer.render("thereIsAProblem.njk").map(ServiceUnavailable(_))
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

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request => result(mode, regime, form)
  }

  private def matchBusinessInfo(implicit request: DataRequest[AnyContent]): Future[Either[ApiError, MatchingInfo]] =
    (for {
      utr <- request.userAnswers.get(UTRPage)
      businessName <- request.userAnswers
        .get(BusinessNamePage)
        .orElse(request.userAnswers.get(SoleNamePage).map {
          name => s"${name.firstName} ${name.lastName}"
        })
      businessType <- request.userAnswers.get(BusinessTypePage)
    } yield matchingService.sendBusinessMatchingInformation(utr, businessName, businessType))
      .getOrElse(Future.successful(Left(MandatoryInformationMissingError)))

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
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
