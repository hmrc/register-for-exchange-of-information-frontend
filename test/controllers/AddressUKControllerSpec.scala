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

import base.{ControllerNoDataSpecBase, ControllerSpecBase}
import forms.{AddressUKFormProvider, AddressWithoutIdFormProvider}
import models.{Address, Country, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.AddressUKPage
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class AddressUKControllerSpec extends ControllerNoDataSpecBase {
  lazy val loadRoute   = routes.AddressUKController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.AddressUKController.onSubmit(NormalMode).url

  val formProvider        = new AddressUKFormProvider()
  val form: Form[Address] = formProvider(Seq(Country("valid", "GB", "United Kingdom")))
  val address: Address    = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), Country("valid", "GB", "United Kingdom"))

  val userAnswers = UserAnswers(userAnswersId).set(AddressUKPage, address).success.value

  "AddressWithoutId Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(GET, loadRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> form,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "addressUK.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, loadRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(
        Map(
          "addressLine1" -> "value 1",
          "addressLine2" -> "value 2",
          "addressLine3" -> "value 3",
          "addressLine4" -> "value 4",
          "postCode"     -> "XX9 9XX",
          "country"      -> "GB"
        )
      )
      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "addressUK.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(userAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("addressLine1", "value 1"),
                                  ("addressLine2", "value 2"),
                                  ("addressLine3", "value 3"),
                                  ("addressLine4", "value 4"),
                                  ("postCode", "XX9 9XX"),
                                  ("country", "GB")
          )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("field1", ""))
      val boundForm      = form.bind(Map("field1" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "addressUK.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }
  }
}