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
import controllers.{routes, WithEitherT}
import models.error.ApiError
import models.error.ApiError.{DuplicateSubmissionError, MandatoryInformationMissingError}
import models.matching.MatchingType.AsIndividual
import models.matching.RegistrationInfo
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{CheckMode, Mode, Name, Regime}
import pages._
import play.api.mvc.{AnyContent, Call}
import repositories.SessionRepository
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector, sessionRepository: SessionRepository)(implicit ec: ExecutionContext)
    extends WithEitherT {

  def onBusinessMatch(mode: Mode, regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Call] =
    (for {
      registrationInfo <- buildBusinessRegistrationInfo
      orPreviousInfo <- getEither(RegistrationInfoPage).recover {
        case _ => registrationInfo
      }
      //        _ = println("\n\n>>> " + orPreviousInfo.sameAs(registrationInfo))
      //        _ = println("\n\n>> " + EitherT.cond[Future](!orPreviousInfo.sameAs(registrationInfo), orPreviousInfo, DuplicateSubmissionError))
      orError  <- EitherT.cond[Future](!orPreviousInfo.sameAs(registrationInfo), orPreviousInfo, DuplicateSubmissionError)
      response <- EitherT(sendBusinessRegistrationInformation(regime, orError))
      withInfo <- setEither(RegistrationInfoPage, response)
      _ = sessionRepository.set(withInfo)
    } yield routes.IsThisYourBusinessController.onPageLoad(mode, regime)).valueOr {
      case DuplicateSubmissionError if mode == CheckMode =>
        routes.CheckYourAnswersController.onPageLoad(regime)
      case error =>
//        logger.debug(s"Business not matched with error $error")
        routes.NoRecordsMatchedController.onPageLoad(regime)
    }

  private def buildBusinessRegistrationInfo(implicit request: DataRequest[AnyContent]): EitherT[Future, ApiError, RegistrationInfo] =
    for {
      utr          <- getEither(UTRPage)
      businessName <- getEither(BusinessNamePage).orElse(getEither(SoleNamePage).map(_.fullName))
      businessType <- getEither(BusinessTypePage)
      dateOfBirth = request.userAnswers.get(SoleDateOfBirthPage)
    } yield RegistrationInfo.build(businessType, businessName, utr, dateOfBirth)

  def sendIndividualRegistratonInformation(regime: Regime, nino: Nino, name: Name, dob: LocalDate)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withIndividualNino(RegisterWithID(regime, name, dob, "NINO", nino.nino))
      .subflatMap {
        response =>
          response.safeId
            .map {
              RegistrationInfo.build(_, AsIndividual)
            }
            .toRight(MandatoryInformationMissingError())
      }
      .value

  def sendBusinessRegistrationInformation(regime: Regime, registrationInfo: RegistrationInfo)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withOrganisationUtr(RegisterWithID(regime, registrationInfo))
      .subflatMap {
        response =>
          (for {
            safeId <- response.safeId
            name    = response.organisationName
            address = response.address
          } yield registrationInfo.copy(safeId = safeId, name = name, address = address)).toRight(MandatoryInformationMissingError())
      }
      .value
}
