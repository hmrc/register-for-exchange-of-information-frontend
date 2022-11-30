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
import forms.SelectAddressFormProvider
import models.{AddressLookup, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.AddressLookupPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.SelectAddressView

import scala.concurrent.Future

class SelectAddressControllerSpec extends SpecBase with ControllerMockFixtures {

  override def onwardRoute: Call = Call("GET", "/foo")

  lazy val selectAddressRoute: String = routes.SelectAddressController.onPageLoad(NormalMode).url

  val formProvider       = new SelectAddressFormProvider()
  val form: Form[String] = formProvider()

  val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )

  val addressRadios: Seq[RadioItem] = Seq(
    RadioItem(content = Text("1 Address line 1, Town, ZZ1 1ZZ"), value = Some("1 Address line 1, Town, ZZ1 1ZZ")),
    RadioItem(content = Text("2 Address line 1, Town, ZZ1 1ZZ"), value = Some("2 Address line 1, Town, ZZ1 1ZZ"))
  )

  val userAnswers = UserAnswers(userAnswersId)
    .set(AddressLookupPage, addresses)
    .success
    .value

  "SelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, addressRadios, NormalMode).toString
      }
    }

    "must redirect to manual UK address page if there are no address matches" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val request = FakeRequest(GET, selectAddressRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.AddressUKController.onPageLoad(NormalMode).url
    }

    "must redirect to the next page when valid data is submitted" in {

      retrieveUserAnswersData(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = guiceApplicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute).withFormUrlEncodedBody(("value", "1 Address line 1, Town, ZZ1 1ZZ"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, addressRadios, NormalMode).toString
      }
    }

  }
}
