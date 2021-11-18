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
import models.matching.RegistrationInfo
import models.register.request.RegisterWithID
import models.{Name, Regime}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector) {

  //def sendIndividualRegistratonInformation(regime: Regime, nino: Nino, name: Name, dob: LocalDate)(implicit
  def sendIndividualRegistratonInformation(regime: Regime, registrationInfo: RegistrationInfo)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, RegistrationInfo]] =
    registrationConnector
      .withIndividualNino(RegisterWithID(regime, registrationInfo))
      .subflatMap {
        response =>
          response.safeId
          (for {
            safeId <- response.safeId
            name    = response.organisationName
            address = response.address
            nino <- response.registerWithIDResponse.responseDetail.get.
            // RegistrationInfo("", Option(name), None, None, Option(nino), dob)
          } yield RegistrationInfo(safeId, name, address, AsIndividual)).toRight(MandatoryInformationMissingError())
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
