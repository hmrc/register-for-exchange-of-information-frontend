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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import models.ReporterType
import models.ReporterType.Sole
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.ReporterTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SoleTraderNotIdentifiedView

class SoleTraderNotIdentifiedControllerSpec extends SpecBase with ControllerMockFixtures with TableDrivenPropertyChecks {

  "SoleTraderNotIdentified Controller" - {

    lazy val selfAssessmentEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    val indexUrl = routes.IndexController.onPageLoad().url

    "return OK and the correct view with link for self assessment enquiries for a GET as a Sole reporterType" in {

      val userAnswers = emptyUserAnswers.set(ReporterTypePage, Sole).success.value
      retrieveUserAnswersData(userAnswers)
      val application = guiceApplicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, routes.SoleTraderNotIdentifiedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SoleTraderNotIdentifiedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(selfAssessmentEnquiriesLink, indexUrl)(request, messages(application)).toString

      }
    }
  }

  private val disallowedReporterTypes = Table(
    "disallowedReporterType",
    ReporterType.values.filter(_ != Sole): _*
  )

  forAll(disallowedReporterTypes) {
    reporterType =>
      s"redirect to ThereIsAProblemPage for a GET as a $reporterType reporterType" in {

        val userAnswers = emptyUserAnswers.set(ReporterTypePage, reporterType).success.value
        retrieveUserAnswersData(userAnswers)
        val application = guiceApplicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.SoleTraderNotIdentifiedController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ThereIsAProblemController.onPageLoad().url)

        }
      }
  }
}
