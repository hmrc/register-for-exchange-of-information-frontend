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

import cats.implicits._
import connectors.RegistrationConnector
import models.matching.MatchingInfo
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.register.request.RegisterWithID
import models.{BusinessType, Name}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector) {

  type MatchingResponseType[T] = Future[Either[ApiError, T]]

  def sendIndividualMatchingInformation(nino: Nino, name: Name, dob: LocalDate)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): MatchingResponseType[MatchingInfo] =
    registrationConnector
      .withIndividualNino(RegisterWithID(name, dob, "NINO", nino.nino))
      .subflatMap {
        response =>
          response.safeId
            .map {
              MatchingInfo(_, None, None)
            }
            .toRight(MandatoryInformationMissingError)
      }
      .value

  def sendBusinessMatchingInformation(utr: String, businessName: String, businessType: BusinessType)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): MatchingResponseType[MatchingInfo] =
    registrationConnector
      .withOrganisationUtr(RegisterWithID(businessName, businessType, "UTR", utr))
      .subflatMap {
        response =>
          (for {
            safeId <- response.safeId
            name    = response.organisationName
            address = response.address
          } yield MatchingInfo(safeId, name, address)).toRight(MandatoryInformationMissingError)
      }
      .value
}
