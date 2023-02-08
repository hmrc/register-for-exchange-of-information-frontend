/*
 * Copyright 2023 HM Revenue & Customs
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
import models.error.ApiError.NotFoundError
import models.matching.{OrgRegistrationInfo, RegistrationRequest}
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{BusinessType, Mode}
import navigation.MDRNavigator
import pages._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.{BusinessMatchingWithIdService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{IsThisYourBusinessView, ThereIsAProblemView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  standardActionSets: StandardActionSets,
  formProvider: IsThisYourBusinessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingWithIdService,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  view: IsThisYourBusinessView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  private def result(mode: Mode, form: Form[Boolean], registrationInfo: OrgRegistrationInfo)(implicit
    ec: ExecutionContext,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.getDisplaySubscriptionId(registrationInfo.safeId) flatMap {
      case Some(subscriptionId) => controllerHelper.updateSubscriptionIdAndCreateEnrolment(registrationInfo.safeId, subscriptionId)
      case _ =>
        val preparedForm = request.userAnswers.get(IsThisYourBusinessPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, registrationInfo, mode)))
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      buildRegisterWithId() match {
        case Some(registerWithID) =>
          matchingService.sendBusinessRegistrationInformation(registerWithID).flatMap {
            case Right(response) =>
              request.userAnswers.set(RegistrationInfoPage, response).map(sessionRepository.set)
              result(mode, form, response)
            case Left(NotFoundError) =>
              Future.successful(Redirect(routes.BusinessNotIdentifiedController.onPageLoad()))
            case _ =>
              Future.successful(InternalServerError(errorView()))
          }
        case _ =>
          Future.successful(InternalServerError(errorView()))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val thereIsAProblem = Future.successful(InternalServerError(errorView()))
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers
              .get(RegistrationInfoPage)
              .fold(thereIsAProblem) {
                case registrationInfo: OrgRegistrationInfo =>
                  Future.successful(BadRequest(view(formWithErrors, registrationInfo, mode)))
                case _ => thereIsAProblem
              },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, updatedAnswers))
        )
  }

  def buildRegisterWithId()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    request.userAnswers.get(BusinessTypePage) flatMap {
      case BusinessType.Sole => buildIndividualRegistrationRequest()
      case _                 => buildBusinessRegistrationRequest()
    }

  def buildBusinessRegistrationRequest()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr          <- request.userAnswers.get(UTRPage)
      businessName <- request.userAnswers.get(BusinessNamePage)
      businessType = request.userAnswers.get(BusinessTypePage)
    } yield RegisterWithID(RegistrationRequest("UTR", utr.uniqueTaxPayerReference, businessName, businessType, None))

  def buildIndividualRegistrationRequest()(implicit request: DataRequest[AnyContent]): Option[RegisterWithID] =
    for {
      utr             <- request.userAnswers.get(UTRPage)
      soleTradersName <- request.userAnswers.get(SoleNamePage)
    } yield RegisterWithID(soleTradersName, None, "UTR", utr.uniqueTaxPayerReference)

}
