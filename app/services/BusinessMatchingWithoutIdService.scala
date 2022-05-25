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

import connectors.RegistrationConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.SafeId
import models.register.request.RegisterWithoutID
import models.requests.DataRequest
import models.shared.ContactDetails
import models.{Address, Name}
import pages._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingWithoutIdService @Inject() (registrationConnector: RegistrationConnector)(implicit ec: ExecutionContext) {

  def registerWithoutId()(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    request.userAnswers.get(DoYouHaveNINPage) match {
      case Some(false) => individualRegistration()
      case _           => businessRegistration()
    }

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
              .get(IndividualAddressWithoutIdPage) // orElse ?
              .fold(request.userAnswers.get(AddressUKPage))(Some.apply)
        }
      case _ => request.userAnswers.get(AddressUKPage)
    }

  private val registrationError = Future.successful(Left(MandatoryInformationMissingError()))

  private def individualRegistration()(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    (for {
      name <- buildIndividualName
      dob  <- request.userAnswers.get(WhatIsYourDateOfBirthPage).orElse(request.userAnswers.get(DateOfBirthWithoutIdPage))
      phoneNumber  = request.userAnswers.get(IndividualContactPhonePage)
      emailAddress = request.userAnswers.get(IndividualContactEmailPage)
      address <- buildIndividualAddress
    } yield sendIndividualRegistration(name, dob, address, ContactDetails(phoneNumber, emailAddress))).getOrElse(registrationError)

  private def businessRegistration()(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    (for {
      organisationName <- request.userAnswers.get(BusinessWithoutIDNamePage)
      phoneNumber  = request.userAnswers.get(ContactPhonePage)
      emailAddress = request.userAnswers.get(ContactEmailPage)
      address <- request.userAnswers.get(BusinessAddressWithoutIdPage)
    } yield sendBusinessRegistration(organisationName, address, ContactDetails(phoneNumber, emailAddress)))
      .getOrElse(registrationError)

  def sendIndividualRegistration(name: Name, dob: LocalDate, address: Address, contactDetails: ContactDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, SafeId]] =
    registrationConnector
      .withIndividualNoId(RegisterWithoutID(name, dob, address, contactDetails))

  def sendBusinessRegistration(businessName: String, address: Address, contactDetails: ContactDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, SafeId]] =
    registrationConnector
      .withOrganisationNoId(RegisterWithoutID(businessName, address, contactDetails))

}
