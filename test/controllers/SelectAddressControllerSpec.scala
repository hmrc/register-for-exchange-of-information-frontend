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

package controllers

import base.{ControllerMockFixtures, SpecBase}
import forms.SelectAddressFormProvider
import models.{AddressLookup, MDR, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.{AddressLookupPage, SelectAddressPage}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.Radios

import scala.concurrent.Future

class SelectAddressControllerSpec extends SpecBase with ControllerMockFixtures {

  override def onwardRoute: Call = Call("GET", "/foo")

  val manualAddressURL: String        = "/register-for-exchange-of-information/register/mdr/without-id/address-uk"
  lazy val selectAddressRoute: String = routes.SelectAddressController.onPageLoad(NormalMode, MDR).url

  val formProvider       = new SelectAddressFormProvider()
  val form: Form[String] = formProvider()

  // todo tak testujemy
  val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )

  val addressRadios: Seq[Radios.Radio] = Seq(
    Radios.Radio(label = msg"1 Address line 1, Town, ZZ1 1ZZ", value = s"1 Address line 1, Town, ZZ1 1ZZ"),
    Radios.Radio(label = msg"2 Address line 1, Town, ZZ1 1ZZ", value = s"2 Address line 1, Town, ZZ1 1ZZ")
  )

  "SelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val answers = UserAnswers(userAnswersId)
        .set(AddressLookupPage, addresses)
        .success
        .value

      retrieveUserAnswersData(answers)

      val request        = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"             -> form,
        "mode"             -> NormalMode,
        "manualAddressUrl" -> manualAddressURL,
        "radios"           -> Radios(field = form("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = UserAnswers(userAnswersId)
        .set(SelectAddressPage, "1 Address line 1, Town, ZZ1 1ZZ")
        .success
        .value
        .set(AddressLookupPage, addresses)
        .success
        .value

      retrieveUserAnswersData(userAnswers)

      val request        = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> addressRadios.head.value))

      val expectedJson = Json.obj(
        "form"             -> filledForm,
        "mode"             -> NormalMode,
        "manualAddressUrl" -> manualAddressURL,
        "radios"           -> Radios(field = filledForm("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }

    "must redirect to manual UK address page if there are no address matches" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val request = FakeRequest(GET, selectAddressRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.AddressUKController.onPageLoad(NormalMode, MDR).url
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(AddressLookupPage, addresses)
        .success
        .value

      retrieveUserAnswersData(userAnswers)

      val request =
        FakeRequest(POST, selectAddressRoute)
          .withFormUrlEncodedBody(("value", "1 Address line 1, Town, ZZ1 1ZZ"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = UserAnswers(userAnswersId)
        .set(AddressLookupPage, addresses)
        .success
        .value

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(POST, selectAddressRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"             -> boundForm,
        "mode"             -> NormalMode,
        "manualAddressUrl" -> manualAddressURL,
        "radios"           -> Radios(field = boundForm("value"), items = addressRadios)
      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
