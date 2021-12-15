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

package services

import cats.data.EitherT
import cats.implicits._
import connectors.RegistrationConnector
import controllers.routes
import models.error.ApiError
import models.error.ApiError.{DuplicateSubmissionError, MandatoryInformationMissingError}
import models.matching.MatchingType.{AsIndividual, AsOrganisation}
import models.matching.{RegistrationInfo, RegistrationRequest}
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{BusinessType, CheckMode, Mode, Regime}
import pages._
import play.api.mvc.{AnyContent, Call}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector, sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  def onBusinessMatch(mode: Mode, regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Call] =
    (for {
      registrationRequest <- EitherT.fromEither[Future](buildBusinessRegistrationRequest)
      response            <- EitherT(sendBusinessRegistrationInformation(regime, registrationRequest))
      withInfo            <- EitherT.fromEither[Future](request.userAnswers.setEither(RegistrationInfoPage, response))
      _                   <- EitherT.fromEither[Future](Right[ApiError, Future[Boolean]](sessionRepository.set(withInfo)))
    } yield routes.IsThisYourBusinessController.onPageLoad(mode, regime)).valueOr {
      case DuplicateSubmissionError if mode == CheckMode =>
        routes.CheckYourAnswersController.onPageLoad(regime)
      case error =>
        routes.NoRecordsMatchedController.onPageLoad(regime)
    }

  def buildBusinessName(implicit request: DataRequest[AnyContent]): Option[String] =
    request.userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.Sole) => request.userAnswers.get(SoleNamePage).map(_.fullName)
      case _                       => request.userAnswers.get(BusinessNamePage)
    }

  def buildBusinessRegistrationRequest(implicit request: DataRequest[AnyContent]): Either[ApiError, RegistrationRequest] =
    (for {
      utr          <- request.userAnswers.get(UTRPage)
      businessName <- buildBusinessName
      businessType = request.userAnswers.get(BusinessTypePage)
      dateOfBirth  = request.userAnswers.get(SoleDateOfBirthPage)
    } yield RegistrationRequest("UTR", utr.uniqueTaxPayerReference, businessName, businessType, dateOfBirth))
      .toRight(MandatoryInformationMissingError())

  def sendBusinessRegistrationInformation(regime: Regime, registrationRequest: RegistrationRequest)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withOrganisationUtr(RegisterWithID(regime, registrationRequest))
      .subflatMap {
        response =>
          (for {
            safeId <- response.safeId
            name    = response.name
            address = response.address
          } yield RegistrationInfo(safeId, name, address, AsOrganisation)).toRight(MandatoryInformationMissingError())
      }
      .value

  def sendIndividualRegistrationInformation(regime: Regime, registrationRequest: RegistrationRequest)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withIndividualNino(RegisterWithID(regime, registrationRequest))
      .subflatMap {
        response =>
          response.safeId
            .map {
              safeId => RegistrationInfo(safeId, None, None, AsIndividual)
            }
            .toRight(MandatoryInformationMissingError())
      }
      .value
}
