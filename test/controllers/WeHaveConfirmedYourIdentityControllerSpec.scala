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
import models.matching.MatchingInfo
import models.register.error.ApiError.NotFoundError
import models.{Name, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.{WhatIsYourDateOfBirthPage, WhatIsYourNamePage, WhatIsYourNationalInsuranceNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.BusinessMatchingService
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.concurrent.Future

class WeHaveConfirmedYourIdentityControllerSpec extends SpecBase with ControllerMockFixtures {

  val validUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(WhatIsYourNationalInsuranceNumberPage, Nino("CC123456C"))
    .success
    .value
    .set(WhatIsYourNamePage, Name("First", "Last"))
    .success
    .value
    .set(WhatIsYourDateOfBirthPage, LocalDate.now())
    .success
    .value

  val mockMatchingService: BusinessMatchingService = mock[BusinessMatchingService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[BusinessMatchingService].toInstance(mockMatchingService)
      )

  override def beforeEach: Unit = {
    reset(mockMatchingService)
    super.beforeEach
  }

  "WeHaveConfirmedYourIdentity Controller" - {

    "return OK and the correct view for a GET when there is a match" in {

      when(mockMatchingService.sendIndividualMatchingInformation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(MatchingInfo("safeId"))))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(validUserAnswers)
      val request        = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "weHaveConfirmedYourIdentity.njk"
    }

    "return redirect for a GET when there is no match" in {

      when(mockMatchingService.sendIndividualMatchingInformation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(NotFoundError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WeCouldNotConfirmController.onPageLoad().url
    }

    "return redirect for a GET when there is no data" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WeCouldNotConfirmController.onPageLoad().url
    }
  }
}
