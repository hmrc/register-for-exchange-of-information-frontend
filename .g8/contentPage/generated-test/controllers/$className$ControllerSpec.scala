package controllers

import base.ControllerSpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View

class $className$ControllerSpec extends ControllerSpecBase {

  val loadRoute: String = ???
  val submitRoute: String = ???

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {
        implicit val request = FakeRequest(GET, routes.$className$Controller.onPageLoad().url)

        val result = route(app, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view().toString
      }
    }
  }
}
