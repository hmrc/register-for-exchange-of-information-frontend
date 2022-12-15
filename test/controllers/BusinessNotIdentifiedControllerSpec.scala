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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import models.BusinessType.{LimitedCompany, Sole}
import models.NormalMode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.BusinessTypePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class BusinessNotIdentifiedControllerSpec extends SpecBase with ControllerMockFixtures {

  "NoRecordsMatched Controller" - {

    lazy val corporationTaxEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/corporation-tax-enquiries"
    lazy val selfAssessmentEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"
    val startUrl = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode).url

    "return OK and the correct view for a GET when a Limited Company" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(BusinessTypePage, LimitedCompany).success.value

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl" -> corporationTaxEnquiriesLink,
        "lostUtrUrl" -> "https://www.gov.uk/find-lost-utr-number",
        "startUrl"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode).url
      )

      templateCaptor.getValue mustEqual "businessNotIdentified.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }

    "return OK and the correct view for a GET when a Sole Trader" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(BusinessTypePage, Sole).success.value

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, routes.BusinessNotIdentifiedController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl" -> selfAssessmentEnquiriesLink,
        "lostUtrUrl" -> "https://www.gov.uk/find-lost-utr-number",
        "startUrl"   -> routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(NormalMode).url
      )

      templateCaptor.getValue mustEqual "businessNotIdentified.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }
  }
}
