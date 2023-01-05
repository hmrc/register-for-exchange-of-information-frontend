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

package navigation

import controllers.routes
import models._
import pages._
import play.api.libs.json.Reads
import play.api.mvc.Call

trait Navigator {

  val normalRoutes: Page => UserAnswers => Option[Call]

  val checkRouteMap: Page => UserAnswers => Option[Call]

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.IndexController.onPageLoad()
      }

    case CheckMode =>
      checkRouteMap(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.IndexController.onPageLoad()
      }
  }

  def checkNextPageForValueThenRoute[A](mode: Mode, ua: UserAnswers, page: QuestionPage[A], call: Call)(implicit rds: Reads[A]): Option[Call] =
    if (
      mode.equals(CheckMode) && ua
        .get(page)
        .fold(false)(
          _ => true
        )
    ) {
      Some(routes.CheckYourAnswersController.onPageLoad())
    } else {
      Some(call)
    }
}

object Navigator {

  val missingInformation: Call = controllers.routes.SomeInformationIsMissingController.onPageLoad()
  val checkYourAnswers: Call   = controllers.routes.CheckYourAnswersController.onPageLoad()
}
