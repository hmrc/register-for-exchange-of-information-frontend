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

import cats.implicits.catsStdInstancesForFuture
import connectors.RegistrationConnector
import controllers.WithEitherT
import models.BusinessType.Sole
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.error.ApiError
import models.error.ApiError.{MandatoryInformationMissingError, RegistrationResponseType}
import models.matching.MatchingType.{AsIndividual, AsOrganisation}
import models.matching.RegistrationInfo
import models.register.request.RegisterWithoutID
import models.requests.DataRequest
import models.shared.ContactDetails
import models.{Address, Name, Regime}
import pages._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationService @Inject() (registrationConnector: RegistrationConnector)(implicit ec: ExecutionContext) extends WithEitherT {

  def isRegisteringAsBusiness()(implicit request: DataRequest[AnyContent]): Boolean =
    (request.userAnswers.get(WhatAreYouRegisteringAsPage),
     request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
     request.userAnswers.get(BusinessTypePage)
    ) match { //ToDo defaulting to registering for business change when paths created if necessary
      case (None, Some(true), Some(Sole))                   => false
      case (None, Some(true), _)                            => true
      case (Some(RegistrationTypeBusiness), Some(false), _) => true
      case _                                                => false
    }

  def registerWithoutId(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, RegistrationInfo]] =
    businessRegistration(regime).orElse(individualRegistration(regime)).value

  private def individualRegistration(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): RegistrationResponseType =
    for {
      name <- getEither(WhatIsYourNamePage).orElse(getEither(NonUkNamePage).map(_.toName))
      dob  <- getEither(WhatIsYourDateOfBirthPage)
      phoneNumber  = request.userAnswers.get(ContactPhonePage)
      emailAddress = request.userAnswers.get(ContactEmailPage)
      address  <- getEither(AddressWithoutIdPage).orElse(getEither(AddressUKPage)).orElse(getEither(SelectedAddressLookupPage) subflatMap (_.toAddress))
      response <- sendIndividualRegistration(regime, name, dob, address, ContactDetails(phoneNumber, emailAddress))
    } yield response

  private def businessRegistration(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): RegistrationResponseType =
    for {
      organisationName <- getEither(BusinessWithoutIDNamePage)
      phoneNumber  = request.userAnswers.get(ContactPhonePage)
      emailAddress = request.userAnswers.get(ContactEmailPage)
      address  <- getEither(AddressWithoutIdPage)
      response <- sendBusinessRegistration(regime, organisationName, address, ContactDetails(phoneNumber, emailAddress))
    } yield response

  def sendIndividualRegistration(regime: Regime, name: Name, dob: LocalDate, address: Address, contactDetails: ContactDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): RegistrationResponseType =
    registrationConnector
      .withIndividualNoId(RegisterWithoutID(regime, name, dob, address, contactDetails))
      .subflatMap {
        response =>
          (for {
            safeId <- response.registerWithoutIDResponse.safeId
          } yield RegistrationInfo(safeId, None, None, AsIndividual)).toRight(MandatoryInformationMissingError())
      }

  def sendBusinessRegistration(regime: Regime, businessName: String, address: Address, contactDetails: ContactDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): RegistrationResponseType =
    registrationConnector
      .withOrganisationNoId(RegisterWithoutID(regime, businessName, address, contactDetails))
      .subflatMap {
        response =>
          (for {
            safeId <- response.registerWithoutIDResponse.safeId
          } yield RegistrationInfo(safeId, None, None, AsOrganisation)).toRight(MandatoryInformationMissingError())
      }
}
