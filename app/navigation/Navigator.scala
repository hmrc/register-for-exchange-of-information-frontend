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

package navigation

import controllers.routes
import models._
import pages._
import play.api.libs.json.Reads
import play.api.mvc.Call

trait Navigator {

  val normalRoutes: Page => Regime => UserAnswers => Option[Call]

  val checkRouteMap: Page => Regime => UserAnswers => Option[Call]

  def nextPage(page: Page, mode: Mode, regime: Regime, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(regime)(userAnswers) match {
        case Some(call) => call
        case None       => routes.IndexController.onPageLoad(regime)
      }

    case CheckMode =>
      checkRouteMap(page)(regime)(userAnswers) match {
        case Some(call) => call
        case None       => routes.IndexController.onPageLoad(regime)
      }
  }

  def checkNextPageForValueThenRoute[A](mode: Mode, regime: Regime, ua: UserAnswers, page: QuestionPage[A], call: Call)(implicit rds: Reads[A]): Option[Call] =
    if (
      mode.equals(CheckMode) && ua
        .get(page)
        .fold(false)(
          _ => true
        )
    ) {
      Some(routes.CheckYourAnswersController.onPageLoad(regime))
    } else {
      Some(call)
    }

  // Not anymore from 7/12: In CHECKMODE if new user answer matches old user answer redirect to CYA page
  // Now, if new value differs from old value, then go to normal mode - if in NM, it will have no effect, in CM will force full journey
  def nextPageWithValueCheck[A](page: QuestionPage[A], mode: Mode, regime: Regime, userAnswers: UserAnswers, originalValue: Option[A])(implicit
    rds: Reads[A]
  ): Call = {

    val valueMatchesOriginalAnswer = userAnswers
      .get(page)
      .fold(false)(
        newValue => newValue.equals(originalValue.getOrElse(false))
      )

    mode match {
      case NormalMode =>
        normalRoutes(page)(regime)(userAnswers) match {
          case Some(call) => call
          case None       => routes.IndexController.onPageLoad(regime)
        }

      case CheckMode =>
        checkRouteMap(page)(regime)(userAnswers) match {
          case Some(_) if valueMatchesOriginalAnswer =>
            routes.CheckYourAnswersController.onPageLoad(regime)
          case Some(call) => call
          case None       => routes.IndexController.onPageLoad(regime)
        }
    }
  }

  def isContinueJourney[A](page: QuestionPage[A], mode: Mode, ua: UserAnswers)(implicit reads: Reads[A]): Boolean =
    (ua.get(page), mode) match {
      case (Some(_), CheckMode) => false
      case _                    => true
    }
}

object Navigator {

  val missingInformation: Regime => Call = (regime: Regime) => controllers.routes.SomeInformationIsMissingController.onPageLoad(regime)
  val checkYourAnswers: Regime => Call   = (regime: Regime) => controllers.routes.CheckYourAnswersController.onPageLoad(regime)
}
