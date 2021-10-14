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
import models.{Mode, UniqueTaxpayerReference}
import models.matching.MatchingInfo
import models.register.error.ApiError
import models.register.error.ApiError.{MandatoryInformationMissingError, NotFoundError}
import models.requests.DataRequest
import navigation.MDRNavigator
import pages.{
  BusinessNamePage,
  BusinessTypePage,
  IsThisYourBusinessPage,
  SoleNamePage,
  UTRPage,
  WhatIsYourDateOfBirthPage,
  WhatIsYourNamePage,
  WhatIsYourNationalInsuranceNumberPage
}
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

  private def render(mode: Mode, form: Form[Boolean])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"   -> form,
      "action" -> routes.IsThisYourBusinessController.onSubmit(mode).url,
      "radios" -> Radios.yesNo(form("value"))
    )
    renderer.render("isThisYourBusiness.njk", data)
  }

  // TODO clean-up before PR
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      matchBusinessInfo flatMap {
        case Right(_) =>
          render(mode, request.userAnswers.get(IsThisYourBusinessPage).fold(form)(form.fill)).map(Ok(_))
        case Left(NotFoundError) =>
          Future.successful(Redirect(routes.WeCouldNotConfirmController.onPageLoad("organisation")))
        case _ =>
          Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
      }
  }

  private def matchBusinessInfo(implicit request: DataRequest[AnyContent]): Future[Either[ApiError, MatchingInfo]] =
    (for {
      utr <- request.userAnswers.get(UTRPage)
      businessName <- request.userAnswers
        .get(BusinessNamePage)
        .orElse(request.userAnswers.get(SoleNamePage).map {
          name => s"${name.firstName} ${name.lastName}"
        })
      //TODO: ETMP data suggests sole trader business partner accounts are individual records
      businessType <- request.userAnswers.get(BusinessTypePage)
    } yield matchingService.sendBusinessMatchingInformation(utr, businessName, businessType))
      .getOrElse(Future.successful(Left(MandatoryInformationMissingError)))

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, updatedAnswers))
        )
  }
}
