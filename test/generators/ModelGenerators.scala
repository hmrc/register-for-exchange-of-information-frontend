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

package generators

import models.email.EmailRequest
import models.subscription.request._
import models.{Address, Country, Regime, UniqueTaxpayerReference}
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

  implicit lazy val arbitraryRegime: Arbitrary[Regime] =
    Arbitrary(Gen.oneOf(Regime.regimes))

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

  implicit lazy val arbitraryContactInformation: Arbitrary[ContactType] = Arbitrary {
    Gen.oneOf[ContactType](arbitrary[OrganisationDetails], arbitrary[IndividualDetails])
  }

  implicit val arbitraryPrimaryContact: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactType]
      email              <- arbitrary[String]
      phone              <- Gen.option(arbitrary[String])
      mobile             <- Gen.option(arbitrary[String])
    } yield ContactInformation(contactInformation, email, phone, mobile)
  }

  implicit val arbitraryCreateRequestDetail: Arbitrary[CreateRequestDetail] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
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

  implicit val arbitraryRequestDetail: Arbitrary[RequestDetail] = Arbitrary {
    for {
      idType   <- arbitrary[String]
      idNumber <- arbitrary[String]
    } yield RequestDetail(idType, idNumber)
  }

  implicit val arbitraryReadSubscriptionRequest: Arbitrary[ReadSubscriptionRequest] = Arbitrary {
    for {
      requestCommon  <- arbitrary[SubscriptionRequestCommon]
      requestDetails <- arbitrary[RequestDetail]
    } yield ReadSubscriptionRequest(requestCommon, requestDetails)
  }

  implicit val arbitraryDisplaySubscriptionForMDRRequest: Arbitrary[DisplaySubscriptionForMDRRequest] = Arbitrary {
    for {
      subscriptionRequest <- arbitrary[ReadSubscriptionRequest]
    } yield DisplaySubscriptionForMDRRequest(subscriptionRequest)
  }

  implicit val arbitraryDisplaySubscriptionForCBCRequest: Arbitrary[DisplaySubscriptionForCBCRequest] = Arbitrary {
    for {
      subscriptionRequest <- arbitrary[ReadSubscriptionRequest]
    } yield DisplaySubscriptionForCBCRequest(subscriptionRequest)
  }

  implicit lazy val arbitraryDisplaySubscriptionRequest: Arbitrary[DisplaySubscriptionRequest] = Arbitrary {
    Gen.oneOf[DisplaySubscriptionRequest](arbitrary[DisplaySubscriptionForMDRRequest], arbitrary[DisplaySubscriptionForCBCRequest])
  }

  implicit lazy val arbitraryUniqueTaxpayerReference: Arbitrary[UniqueTaxpayerReference] = Arbitrary {
    for {
      utr <- arbitrary[String]
    } yield UniqueTaxpayerReference(utr)
  }

  implicit val arbitraryEmailRequest: Arbitrary[EmailRequest] = Arbitrary {
    for {
      to          <- arbitrary[List[String]]
      id          <- arbitrary[String]
      contactName <- arbitrary[Map[String, String]]

    } yield EmailRequest(to, id, contactName)
  }

}
