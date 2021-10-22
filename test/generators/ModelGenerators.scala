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

package generators

import models.subscription.request.{
  ContactInformation,
  CreateRequestDetail,
  CreateSubscriptionForMDRRequest,
  IndividualDetails,
  OrganisationDetails,
  PrimaryContact,
  RequestParameter,
  SecondaryContact,
  SubscriptionRequest,
  SubscriptionRequestCommon
}
import models.{Address, Country}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino

trait ModelGenerators {

  implicit lazy val arbitraryWhatIsTradingName: Arbitrary[models.WhatIsTradingName] =
    Arbitrary {
      for {
        businessName <- arbitrary[String]
      } yield models.WhatIsTradingName(businessName)
    }

  implicit lazy val arbitraryNonUkName: Arbitrary[models.NonUkName] =
    Arbitrary {
      for {
        givenName  <- arbitrary[String]
        familyName <- arbitrary[String]
      } yield models.NonUkName(givenName, familyName)
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        state <- Gen.oneOf(Seq("Valid", "Invalid"))
        code  <- Gen.pick(2, 'A' to 'Z')
        name  <- arbitrary[String]
      } yield Country(state, code.mkString, name)
    }

  implicit lazy val arbitraryAddressWithoutId: Arbitrary[models.Address] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[String]
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[Option[String]]
        country      <- arbitrary[Country]
      } yield Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country)
    }

  implicit lazy val arbitraryName: Arbitrary[models.Name] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield models.Name(firstName, lastName)
    }

  implicit val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      prefix <- Gen.oneOf(Nino.validPrefixes)
      number <- Gen.choose(0, 999999)
      suffix <- Gen.oneOf(Nino.validSuffixes)
    } yield Nino(f"$prefix$number%06d$suffix")
  }

  implicit lazy val arbitraryBussinessType: Arbitrary[models.BusinessType] =
    Arbitrary {
      Gen.oneOf(models.BusinessType.values.toSeq)
    }

  implicit lazy val arbitraryWhatAreYouRegisteringAs: Arbitrary[models.WhatAreYouRegisteringAs] =
    Arbitrary {
      Gen.oneOf(models.WhatAreYouRegisteringAs.values.toSeq)
    }

  implicit val arbitraryRequestParameter: Arbitrary[RequestParameter] = Arbitrary {
    for {
      paramName  <- arbitrary[String]
      paramValue <- arbitrary[String]
    } yield RequestParameter(paramName, paramValue)
  }

  implicit val arbitrarySubscriptionRequestCommon: Arbitrary[SubscriptionRequestCommon] = Arbitrary {
    for {
      regime                   <- arbitrary[String]
      receiptDate              <- arbitrary[String]
      acknowledgementReference <- arbitrary[String]
      originatingSystem        <- arbitrary[String]
      requestParameters        <- Gen.option(arbitrary[Seq[RequestParameter]])
    } yield SubscriptionRequestCommon(regime, receiptDate, acknowledgementReference, originatingSystem, requestParameters)
  }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      organisationName <- arbitrary[String]
    } yield OrganisationDetails(organisationName)
  }

  implicit val arbitraryIndividualDetails: Arbitrary[IndividualDetails] = Arbitrary {
    for {
      firstName  <- arbitrary[String]
      middleName <- Gen.option(arbitrary[String])
      lastName   <- arbitrary[String]
    } yield IndividualDetails(firstName, middleName, lastName)
  }

  implicit lazy val arbitraryContactInformation: Arbitrary[ContactInformation] = Arbitrary {
    Gen.oneOf[ContactInformation](arbitrary[OrganisationDetails], arbitrary[IndividualDetails])
  }

  implicit val arbitraryPrimaryContact: Arbitrary[PrimaryContact] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactInformation]
      email              <- arbitrary[String]
      phone              <- Gen.option(arbitrary[String])
      mobile             <- Gen.option(arbitrary[String])
    } yield PrimaryContact(contactInformation, email, phone, mobile)
  }

  implicit val arbitrarySecondaryContact: Arbitrary[SecondaryContact] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactInformation]
      email              <- arbitrary[String]
      phone              <- Gen.option(arbitrary[String])
      mobile             <- Gen.option(arbitrary[String])
    } yield SecondaryContact(contactInformation, email, phone, mobile)
  }

  implicit val arbitraryCreateRequestDetail: Arbitrary[CreateRequestDetail] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[PrimaryContact]
      secondaryContact <- Gen.option(arbitrary[SecondaryContact])
    } yield CreateRequestDetail(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitrarySubscriptionRequest: Arbitrary[SubscriptionRequest] = Arbitrary {
    for {
      requestCommon  <- arbitrary[SubscriptionRequestCommon]
      requestDetails <- arbitrary[CreateRequestDetail]
    } yield SubscriptionRequest(requestCommon, requestDetails)
  }

  implicit val arbitraryCreateSubscriptionForMDRRequest: Arbitrary[CreateSubscriptionForMDRRequest] = Arbitrary {
    for {
      subscriptionRequest <- arbitrary[SubscriptionRequest]
    } yield CreateSubscriptionForMDRRequest(subscriptionRequest)
  }
}
