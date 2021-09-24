package controllers

import base.{ControllerMockFixtures, SpecBase}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class SomeInformationIsMissingControllerSpec extends SpecBase with ControllerMockFixtures {

  "SomeInformationIsMissing Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, routes.SomeInformationIsMissingController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "someInformationIsMissing.njk"
    }
  }
}
