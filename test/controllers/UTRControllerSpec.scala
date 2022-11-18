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

import base.ControllerSpecBase
import models.{BusinessType, NormalMode, UniqueTaxpayerReference, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.{BusinessTypePage, UTRPage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.UTRView

import scala.concurrent.Future

class UTRControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.UTRController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.UTRController.onSubmit(NormalMode).url

  private def form = new forms.UTRFormProvider().apply("Self Assessment") // has to match BusinessType in user answer

  val userAnswers = UserAnswers(userAnswersId).set(BusinessTypePage, BusinessType.Sole).success.value

  "UTR Controller" - {

    "must return OK and the correct view for a GET when self assessment" in {

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, loadRoute)
      val view           = app.injector.instanceOf[UTRView]
      val taxType        = "Self Assessment"

      val result = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, NormalMode, taxType)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers =
        UserAnswers(userAnswersId).set(BusinessTypePage, BusinessType.Sole).success.value.set(UTRPage, UniqueTaxpayerReference("1234567890")).success.value
      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(GET, loadRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "1234567890"))

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "utr.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(userAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "1234567890"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(userAnswers)
      val request        = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "utr.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to 'SomeInformationIsMissing' when data is missing" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SomeInformationIsMissingController
        .onPageLoad()
        .url
    }
  }
}