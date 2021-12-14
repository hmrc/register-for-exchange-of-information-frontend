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

import base.SpecBase
import generators.Generators
import models.{CheckMode, MDR, NormalMode, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {

    "must check the next page for a value before routing" - {

      val goto: Call = Call("GET", "/next-page")
      val cya: Call  = (Navigator.checkYourAnswers)(MDR)

      case object NextPage extends QuestionPage[String] {
        override def path: JsPath = JsPath \ toString
      }

      "in Normal mode" - {

        "must always go to the specified route" in {

          val userAnswers = UserAnswers(userAnswersId)

          navigator.checkNextPageForValueThenRoute(NormalMode, MDR, userAnswers, NextPage, goto) mustBe Some(goto)
        }
      }

      "in Check mode" - {

        "must go to the next page if that page is clean" in {

          val userAnswers = UserAnswers(userAnswersId)

          navigator.checkNextPageForValueThenRoute(CheckMode, MDR, userAnswers, NextPage, goto) mustBe Some(goto)
        }
      }

      "must go to 'Check your Answers' page if next page has content" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(NextPage, "HAS_CONTENT")
          .success
          .value

        navigator.checkNextPageForValueThenRoute(CheckMode, MDR, userAnswers, NextPage, goto) mustBe Some(cya)
      }
    }
  }

}
