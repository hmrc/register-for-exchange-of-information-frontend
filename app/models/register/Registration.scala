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

package models.register

import models.UserAnswers

//object Registration {
//
//  def apply(userAnswers: UserAnswers): Option[RequestDetails] =
//    userAnswers.get(RegistrationTypePage) match {
//      case Some(models.RegistrationType.Individual) => IndRegistration(userAnswers)
//      case Some(Business)                           => OrgRegistration(userAnswers)
//      case _                                        => throw new SomeInformationIsMissingException("Cannot retrieve registration type")
//    }
//}
//
//object OrgRegistration {
//
//  def apply(userAnswers: UserAnswers): Option[RequestDetails] =
//    for {
//      organisationName <- getBusinessName(userAnswers)
//      addressBusiness  <- userAnswers.get(BusinessAddressPage)
//      address          <- AddressNoId(addressBusiness)
//    } yield RequestDetails(
//      Some(NoIdOrganisation(organisationName)),
//      None,
//      address,
//      ContactBuilder.buildContacts(userAnswers),
//      None
//    )
//
//  private def getBusinessName(userAnswers: UserAnswers): Option[String] =
//    userAnswers.get(BusinessTypePage) match {
//      case Some(BusinessType.NotSpecified) =>
//        userAnswers.get(SoleTraderNamePage).map {
//          name => s"${name.firstName} ${name.secondName}"
//        }
//      case _ => userAnswers.get(BusinessWithoutIDNamePage)
//    }
//}
//
//object IndRegistration {
//
//  def apply(userAnswers: UserAnswers): Option[RequestDetails] = for {
//    name       <- userAnswers.get(NonUkNamePage)
//    dob        <- userAnswers.get(DateOfBirthPage)
//    addressInd <- getAddress(userAnswers)
//    address    <- AddressNoId(addressInd)
//  } yield RequestDetails(
//    None,
//    Some(Individual(name, dob)),
//    address,
//    ContactBuilder.buildContacts(userAnswers),
//    None
//  )
//
//  private def getAddress(userAnswers: UserAnswers): Option[Address] =
//    userAnswers.get(DoYouLiveInTheUKPage) match {
//      case Some(true)  => userAnswers.get(WhatIsYourAddressUkPage).orElse(toAddress(userAnswers))
//      case Some(false) => userAnswers.get(WhatIsYourAddressPage)
//      case _           => throw new SomeInformationIsMissingException("Cannot get address")
//    }
//
//  private def toAddress(userAnswers: UserAnswers) =
//    userAnswers.get(SelectedAddressLookupPage) map {
//      lookUp =>
//        Address(
//          lookUp.addressLine1.getOrElse(""),
//          lookUp.addressLine2,
//          lookUp.addressLine3.getOrElse(""),
//          lookUp.addressLine4,
//          Some(lookUp.postcode),
//          Country("valid", "UK", "United Kingdom")
//        )
//    }
//}
//
