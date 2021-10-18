package controllers

import base.ControllerSpecBase
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.WhatIsTradingNamePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class WhatIsTradingNameControllerSpec extends ControllerSpecBase {

  lazy val loadRoute   = routes.WhatIsTradingNameController.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.WhatIsTradingNameController.onSubmit(NormalMode).url

  private def form = new forms.WhatIsTradingNameFormProvider().apply()

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      WhatIsTradingNamePage.toString -> Json.obj(
        "businessName" -> "value 1"
      )
    )
  )

  "WhatIsTradingName Controller" - {

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

      templateCaptor.getValue mustEqual "whatIsTradingName.njk"
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
          "business-name" -> "value 1",
          "remove"        -> "value 2"
        )
      )

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "action" -> submitRoute
      )

      templateCaptor.getValue mustEqual "whatIsTradingName.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("business-name", "value 1"), ("remove", "value 2"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "action" -> loadRoute
      )

      templateCaptor.getValue mustEqual "whatIsTradingName.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
