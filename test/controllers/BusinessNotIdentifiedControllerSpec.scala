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
import models.ReporterType.{Individual, Sole}
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.ReporterTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.BusinessNotIdentifiedView

class BusinessNotIdentifiedControllerSpec extends SpecBase with ControllerMockFixtures with TableDrivenPropertyChecks {

  "BusinessNotIdentified Controller" - {

    lazy val findAndUpdateCompanyInfoLink: String = "https://find-and-update.company-information.service.gov.uk/"

    val indexUrl = routes.IndexController.onPageLoad().url

    val organisationReporterTypes = Table(
      "organisationReporterType",
      ReporterType.organisationReporterTypes: _*
    )

    forAll(organisationReporterTypes) { reporterType =>
      s"return OK and the correct view with link to find and update company info for a GET as a $reporterType reporterType" in {

        val userAnswers = emptyUserAnswers.set(ReporterTypePage, reporterType).success.value
        retrieveUserAnswersData(userAnswers)
        val application = guiceApplicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BusinessNotIdentifiedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(findAndUpdateCompanyInfoLink, indexUrl, reporterType)(
            request,
            messages(application)
          ).toString

        }
      }
    }

    val disallowedReporterTypes = Table(
      "disallowedReporterType",
      Sole,
      Individual
    )

    forAll(disallowedReporterTypes) { reporterType =>
      s"redirect to ThereIsAProblemPage for a GET as a $reporterType reporterType" in {

        val userAnswers = emptyUserAnswers.set(ReporterTypePage, reporterType).success.value
        retrieveUserAnswersData(userAnswers)
        val application = guiceApplicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ThereIsAProblemController.onPageLoad().url)

        }
      }
    }
  }
}
