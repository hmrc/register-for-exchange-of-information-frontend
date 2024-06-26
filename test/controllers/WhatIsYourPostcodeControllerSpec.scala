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

import base.ControllerSpecBase
import models.{AddressLookup, NormalMode}
import org.mockito.ArgumentMatchers.any
import pages.WhatIsYourPostcodePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatIsYourPostCodeView

import scala.concurrent.Future

class WhatIsYourPostcodeControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.WhatIsYourPostcodeController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.WhatIsYourPostcodeController.onSubmit(NormalMode).url

  private def form = new forms.WhatIsYourPostcodeFormProvider().apply()

  "WhatIsYourPostcode Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request = FakeRequest(GET, loadRoute)

        val result = route(app, request).value

        val view = application.injector.instanceOf[WhatIsYourPostCodeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers      = emptyUserAnswers.set(WhatIsYourPostcodePage, TestPostCode).success.value
      retrieveUserAnswersData(userAnswers)
      implicit val request = FakeRequest(GET, loadRoute)

      val view = app.injector.instanceOf[WhatIsYourPostCodeView]

      val result     = route(app, request).value
      val filledForm = form.bind(Map("postCode" -> TestPostCode))

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(filledForm, NormalMode).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val addresses: Seq[AddressLookup] = Seq(
        AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
        AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("postCode", "AA1 1AA"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockAddressLookupConnector, times(1)).addressLookupByPostcode(any())(any(), any())

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)
      implicit val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm        = form.bind(Map("value" -> "invalid value"))

      val view = app.injector.instanceOf[WhatIsYourPostCodeView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode).toString

    }

    "must return a Bad Request and errors when postcode is submitted but address lookup does not find a match" in {

      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(Seq.empty[AddressLookup]))

      retrieveUserAnswersData(emptyUserAnswers)
      implicit val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("postCode", "AA1 1AA"))
      val boundForm        = form.bind(Map("postCode" -> "AA1 1AA")).withError("postCode", "whatIsYourPostcode.error.notFound")

      val view = app.injector.instanceOf[WhatIsYourPostCodeView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode).toString

    }
  }
}
