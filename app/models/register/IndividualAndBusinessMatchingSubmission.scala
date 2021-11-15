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

import models.register.request.details.Organisation
import models.{Name, UserAnswers}
import pages._
import play.api.libs.json._

import java.time.LocalDate

trait IndividualAndBusinessMatchingSubmission

case class IndividualMatchingSubmission(regime: String, requiresNameMatch: Boolean, isAnAgent: Boolean, individual: Individual)
    extends IndividualAndBusinessMatchingSubmission

object IndividualMatchingSubmission {

  def apply(userAnswers: UserAnswers): Option[IndividualMatchingSubmission] =
    for {
      name <- userAnswers.get(WhatIsYourNamePage)
      dob  <- userAnswers.get(WhatIsYourDateOfBirthPage)
    } yield IndividualMatchingSubmission("CBC", requiresNameMatch = true, isAnAgent = false, Individual(name, dob))

  implicit val format: OFormat[IndividualMatchingSubmission] = Json.format[IndividualMatchingSubmission]
}

case class Individual(name: Name, dateOfBirth: LocalDate)

object Individual {

  implicit lazy val writes: OWrites[Individual] = OWrites[Individual] {
    individual =>
      Json.obj(
        "firstName"   -> individual.name.firstName,
        "lastName"    -> individual.name.lastName,
        "dateOfBirth" -> individual.dateOfBirth.toString
      )
  }

  implicit lazy val reads: Reads[Individual] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "firstName").read[String] and
        (__ \ "lastName").read[String] and
        (__ \ "dateOfBirth").read[LocalDate]
    )(
      (firstName, secondName, dob) => Individual(Name(firstName, secondName), dob)
    )
  }
}

//orgName between 1 and 105 "^[a-zA-Z0-9 '&\\/]{1,105}$"

case class BusinessMatchingSubmission(regime: String, requiresNameMatch: Boolean, isAnAgent: Boolean, organisation: Organisation)
    extends IndividualAndBusinessMatchingSubmission

object BusinessMatchingSubmission {

  implicit val format: OFormat[BusinessMatchingSubmission] = Json.format[BusinessMatchingSubmission]
}
