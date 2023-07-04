package controllers

import java.time.{LocalDate, ZoneOffset}

import base.ControllerSpecBase
import forms.$className$FormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.$className$Page
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View

import scala.concurrent.Future

class $className$ControllerSpec extends ControllerSpecBase {

  lazy val loadRoute = routes.$className$Controller.onPageLoad(NormalMode).url
  lazy val submitRoute = routes.$className$Controller.onSubmit(NormalMode).url

  val form = new $className$FormProvider().apply()

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, loadRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, submitRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder().build()

      running(application) {

        implicit val request = getRequest()

        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set($className$Page, validAnswer).success.value

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder().build()


      running(application) {

        implicit val request = getRequest()

        val view = application.injector.instanceOf[$className$View]

        val result = route(app, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest()).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set($className$Page, validAnswer).success.value

      retrieveUserAnswersData(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val result = route(app, postRequest).value

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

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode).toString
      }
    }
  }
}
