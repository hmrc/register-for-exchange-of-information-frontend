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
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.MatchingType.{AsIndividual, AsOrganisation}
import models.matching.RegistrationInfo
import models.register.request.RegisterWithID
import models.{BusinessType, Name, Regime}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector) {

  def sendIndividualRegistrationInformation(regime: Regime, nino: Nino, name: Name, dob: LocalDate)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withIndividualNino(RegisterWithID(regime, name, dob, "NINO", nino.nino))
      .subflatMap {
        response =>
          response.safeId
            .map {
              RegistrationInfo(_, None, None, AsIndividual)
            }
            .toRight(MandatoryInformationMissingError())
      }
      .value

  def sendBusinessRegistrationInformation(regime: Regime, utr: String, businessName: String, businessType: BusinessType)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withOrganisationUtr(RegisterWithID(regime, businessName, businessType, "UTR", utr))
      .subflatMap {
        response =>
          (for {
            safeId <- response.safeId
            name    = response.organisationName
            address = response.address
          } yield RegistrationInfo(safeId, name, address, AsOrganisation)).toRight(MandatoryInformationMissingError())
      }
      .value
}
