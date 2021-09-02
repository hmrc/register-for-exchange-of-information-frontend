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

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitrarySecondContactUserAnswersEntry: Arbitrary[(pages.SecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.SecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsContactTelephoneUserAnswersEntry: Arbitrary[(pages.IsContactTelephonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.IsContactTelephonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(pages.ContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactEmailUserAnswersEntry: Arbitrary[(pages.ContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.ContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

}
