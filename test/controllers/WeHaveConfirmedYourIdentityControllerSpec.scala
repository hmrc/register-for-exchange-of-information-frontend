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
import models.error.ApiError.{BadRequestError, NotFoundError, ServiceUnavailableError}
import models.matching.IndRegistrationInfo
import models.{Name, NormalMode, SubscriptionID, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.{WhatIsYourDateOfBirthPage, WhatIsYourNamePage, WhatIsYourNationalInsuranceNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{BusinessMatchingWithIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.concurrent.Future

class WeHaveConfirmedYourIdentityControllerSpec extends SpecBase with ControllerMockFixtures {

  val registrationInfo = IndRegistrationInfo(safeId)

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

  val mockMatchingService: BusinessMatchingWithIdService = mock[BusinessMatchingWithIdService]
  val mockSubscriptionService: SubscriptionService       = mock[SubscriptionService]
  val mockTaxEnrolmentService: TaxEnrolmentService       = mock[TaxEnrolmentService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[BusinessMatchingWithIdService].toInstance(mockMatchingService),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService)
      )

  override def beforeEach: Unit = {
    reset(mockMatchingService, mockSubscriptionService, mockTaxEnrolmentService)
    super.beforeEach
  }

  "WeHaveConfirmedYourIdentity Controller" - {

    "return OK and the correct view for a GET when there is a match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request        = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "weHaveConfirmedYourIdentity.njk"
    }

    "must redirect to 'confirmation' page when there is an existing subscription" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))
      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(OK)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RegistrationConfirmationController.onPageLoad().url
    }

    "render technical difficulties page when there is an existing subscription and fails to create an enrolment" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(registrationInfo)))
      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Left(BadRequestError)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request        = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "thereIsAProblem.njk"
    }

    "return redirect for a GET when there is no match" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(NotFoundError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WeCouldNotConfirmController.onPageLoad("identity").url
    }

    "return return Internal Server Error for a GET when an error other than NotFoundError is returned" in {

      when(mockMatchingService.sendIndividualRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(ServiceUnavailableError)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(validUserAnswers)
      val request        = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "thereIsAProblem.njk"
    }

    "return return Internal Server Error for a GET when there is no data" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      retrieveUserAnswersData(emptyUserAnswers)
      val request        = FakeRequest(GET, routes.WeHaveConfirmedYourIdentityController.onPageLoad(NormalMode).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "thereIsAProblem.njk"
    }
  }
}
