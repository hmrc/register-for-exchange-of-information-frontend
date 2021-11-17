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

package utils

import models.{Mode, Regime, UserAnswers}
import models.requests.DataRequest
import pages.{ContactNamePage, QuestionPage}
import play.api.libs.json.Reads
import play.api.mvc.{AnyContent, Call}

trait UserAnswersHelper {

  def isBusinessOrIndividual()(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(ContactNamePage) match {
      case Some(_) => "business"
      case _       => "individual"
    }

  def compareValues[A](newUserValue: A, originalUserValue: Option[A])(implicit rds: Reads[A]): Boolean =
    newUserValue.equals(originalUserValue.getOrElse(false))

}
