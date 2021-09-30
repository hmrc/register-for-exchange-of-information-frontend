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

import models.{Address, Country}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

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

  implicit lazy val arbitraryWhatIsYourName: Arbitrary[models.WhatIsYourName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName  <- arbitrary[String]
      } yield models.WhatIsYourName(firstName, lastName)
    }

  implicit lazy val arbitraryBussinessType: Arbitrary[models.BusinessType] =
    Arbitrary {
      Gen.oneOf(models.BusinessType.values.toSeq)
    }

  implicit lazy val arbitraryWhatAreYouRegisteringAs: Arbitrary[models.WhatAreYouRegisteringAs] =
    Arbitrary {
      Gen.oneOf(models.WhatAreYouRegisteringAs.values.toSeq)
    }
}
