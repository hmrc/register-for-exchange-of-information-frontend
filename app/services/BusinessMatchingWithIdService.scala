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

package services

import cats.implicits._
import connectors.RegistrationConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, RegistrationRequest}
import models.register.request.RegisterWithID
import models.requests.DataRequest
import models.{BusinessType, Regime}
import pages._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingWithIdService @Inject() (registrationConnector: RegistrationConnector) {

  private def buildBusinessName(implicit request: DataRequest[AnyContent]): Option[String] =
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
  ): Future[Either[ApiError, OrgRegistrationInfo]] =
    registrationConnector
      .withOrganisationUtr(RegisterWithID(regime, registrationRequest))
      .subflatMap {
        response =>
          (for {
            safeId  <- response.safeId
            name    <- response.name
            address <- response.address
          } yield OrgRegistrationInfo(safeId, name, address)).toRight(MandatoryInformationMissingError())
      }
      .value

  def sendIndividualRegistrationInformation(regime: Regime, registrationRequest: RegistrationRequest)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, IndRegistrationInfo]] =
    registrationConnector
      .withIndividualNino(RegisterWithID(regime, registrationRequest))
      .subflatMap {
        response =>
          response.safeId
            .map {
              safeId => IndRegistrationInfo(safeId)
            }
            .toRight(MandatoryInformationMissingError())
      }
      .value
}
