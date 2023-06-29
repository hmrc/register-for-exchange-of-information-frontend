package controllers

import base.ControllerSpecBase
import forms.$className$FormProvider
import models.{NormalMode, $className$, UserAnswers}
import pages.$className$Page
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View

import scala.concurrent.Future

class $className$ControllerSpec extends ControllerSpecBase {

  lazy val loadRoute = routes.$className$Controller.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.$className$Controller.onSubmit(NormalMode).url

  val form = new $className$FormProvider().apply()

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      $className$Page.toString -> Json.obj(
        "$field1Name$" -> "value 1",
        "$field2Name$" -> "value 2"
      )
    )
  )

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val view = application.injector.instanceOf[$className$View]

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, loadRoute)

        val view = application.injector.instanceOf[$className$View]

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill($className$("value 1", "value 2")), NormalMode).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)


      retrieveUserAnswersData(emptyUserAnswers)

        val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
       implicit val request =
          FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[$className$View]

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode).toString
      }
    }
  }
}
