/*
 * Copyright 2023 HM Revenue & Customs
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

package models.email

import models.{ReporterType, UserAnswers}
import pages.{AutoMatchedUTR, ReporterTypePage}

import javax.inject.Inject

class EmailUserType @Inject() ()() {

  def getUserTypeFromUa(userAnswers: UserAnswers): UserType =
    userAnswers.get(ReporterTypePage) match {
      case Some(ReporterType.Sole)       => SoleTrader
      case Some(ReporterType.Individual) => Individual
      case None if userAnswers.get(AutoMatchedUTR).isEmpty =>
        throw new RuntimeException("Cannot determine whether the registration should be Organisation or Individual")
      case _ => Organisation
    }
}
