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
import cats.implicits.catsStdInstancesForFuture
import connectors.RegistrationConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
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

class RegistrationService @Inject() (registrationConnector: RegistrationConnector)(implicit ec: ExecutionContext) {

  def registerWithoutId(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, RegistrationInfo]] =
    (request.userAnswers.get(DoYouHaveNINPage) match {
      case Some(false) => individualRegistration(regime)
      case _           => businessRegistration(regime)
    }).value

  private def buildIndividualName(implicit request: DataRequest[AnyContent]): Option[Name] =
    request.userAnswers.get(DoYouHaveNINPage) match {
      case Some(false) => request.userAnswers.get(NonUkNamePage).map(_.toName)
      case _           => request.userAnswers.get(WhatIsYourNamePage)
    }

  private def buildIndividualAddress(implicit request: DataRequest[AnyContent]): Option[Address] =
    request.userAnswers.get(DoYouHaveNINPage) match {
      case Some(false) =>
        request.userAnswers.get(SelectedAddressLookupPage) match {
          case Some(lookup) => lookup.toAddress
          case _ =>
            request.userAnswers
              .get(AddressWithoutIdPage) // orElse ?
              .fold(request.userAnswers.get(AddressUKPage))(Some.apply)
        }
      case _ => request.userAnswers.get(AddressUKPage)
    }

  private val registrationError =
    EitherT[Future, ApiError, RegistrationInfo](Future.successful(Left(MandatoryInformationMissingError())))

  private def individualRegistration(
    regime: Regime
  )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): EitherT[Future, ApiError, RegistrationInfo] =
    (for {
      name <- buildIndividualName
      dob  <- request.userAnswers.get(WhatIsYourDateOfBirthPage)
      phoneNumber  = request.userAnswers.get(ContactPhonePage)
      emailAddress = request.userAnswers.get(ContactEmailPage)
      address <- buildIndividualAddress
    } yield sendIndividualRegistration(regime, name, dob, address, ContactDetails(phoneNumber, emailAddress)))
      .getOrElse(registrationError)

  private def businessRegistration(regime: Regime)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): EitherT[Future, ApiError, RegistrationInfo] =
    (for {
      organisationName <- request.userAnswers.get(BusinessWithoutIDNamePage)
      phoneNumber  = request.userAnswers.get(ContactPhonePage)
      emailAddress = request.userAnswers.get(ContactEmailPage)
      address <- request.userAnswers.get(AddressWithoutIdPage)
    } yield sendBusinessRegistration(regime, organisationName, address, ContactDetails(phoneNumber, emailAddress)))
      .getOrElse(registrationError)

  def sendIndividualRegistration(regime: Regime, name: Name, dob: LocalDate, address: Address, contactDetails: ContactDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): EitherT[Future, ApiError, RegistrationInfo] =
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
  ): EitherT[Future, ApiError, RegistrationInfo] =
    registrationConnector
      .withOrganisationNoId(RegisterWithoutID(regime, businessName, address, contactDetails))
      .subflatMap {
        response =>
          (for {
            safeId <- response.registerWithoutIDResponse.safeId
          } yield RegistrationInfo(safeId, Some(businessName), None, AsOrganisation)).toRight(MandatoryInformationMissingError())
      }
}
