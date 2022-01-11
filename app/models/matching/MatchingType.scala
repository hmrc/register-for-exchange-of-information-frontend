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

package models.matching

import models.{Enumerable, WithName}

sealed trait MatchingType

object MatchingType extends Enumerable.Implicits {

  case object AsIndividual extends WithName("individual") with MatchingType
  case object AsOrganisation extends WithName("organisation") with MatchingType
  val values: Seq[MatchingType] = Seq(AsIndividual, AsOrganisation)

  implicit val enumerable: Enumerable[MatchingType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
