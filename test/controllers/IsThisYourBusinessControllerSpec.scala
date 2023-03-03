/*
 * Copyright 2023 HM Revenue & Customs
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
import models.BusinessType.{LimitedCompany, Sole}
import models.error.ApiError.{BadRequestError, ServiceUnavailableError}
import models.matching.{OrgRegistrationInfo, RegistrationRequest}
import models.register.response.details.AddressResponse
import models.{CheckMode, Name, NormalMode, SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BusinessMatchingWithIdService, SubscriptionService, TaxEnrolmentService}
import views.html.{IsThisYourBusinessView, ThereIsAProblemView}

import scala.concurrent.Future

class IsThisYourBusinessControllerSpec extends SpecBase with ControllerMockFixtures {

  lazy val loadRoute   = routes.IsThisYourBusinessController.onPageLoad(NormalMode).url
  lazy val checkRoute  = routes.IsThisYourBusinessController.onPageLoad(CheckMode).url
  lazy val submitRoute = routes.IsThisYourBusinessController.onSubmit(NormalMode).url

  private def form = new forms.IsThisYourBusinessFormProvider().apply()

  val address             = AddressResponse("line1", None, None, None, None, "GB")
  val registrationRequest = RegistrationRequest("UTR", "UTR", "name", Some(LimitedCompany))
  val registrationInfo    = OrgRegistrationInfo(safeId, "name", address)

  val validUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(BusinessTypePage, LimitedCompany)
    .success
    .value
    .set(UTRPage, utr)
    .success
    .value
    .set(BusinessNamePage, "name")
    .success
    .value
    .set(RegistrationInfoPage, registrationInfo)
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

  override def beforeEach(): Unit = {
    reset(mockMatchingService, mockSubscriptionService, mockTaxEnrolmentService)
    super.beforeEach
  }

  "IsThisYourBusiness Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(OrgRegistrationInfo(safeId, "name", address))))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      retrieveUserAnswersData(validUserAnswers)
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[IsThisYourBusinessView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, registrationInfo, NormalMode).toString
    }

    "must return OK and the correct view for a GET for BusinessType as SoleTrader" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(OrgRegistrationInfo(safeId, "name", address))))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      val updatedUserAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(BusinessTypePage, Sole)
        .success
        .value
        .set(UTRPage, utr)
        .success
        .value
        .set(SoleNamePage, Name("name", "name"))
        .success
        .value

      retrieveUserAnswersData(updatedUserAnswers)
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

      val result: Future[Result] = route(app, request).value

      val view = app.injector.instanceOf[IsThisYourBusinessView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, registrationInfo, NormalMode).toString
    }

    "must redirect to 'confirmation' page when there is an existing subscription" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(OrgRegistrationInfo(safeId, "name", address))))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("Id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Right(OK)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RegistrationConfirmationController.onPageLoad().url

    }

    "render technical difficulties page when there is an existing subscription and fails to create an enrolment" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(OrgRegistrationInfo(safeId, "name", address))))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(Some(SubscriptionID("Id"))))
      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any())).thenReturn(Future.successful(Left(BadRequestError)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Right(OrgRegistrationInfo(safeId, "name", address))))

      when(mockSubscriptionService.getDisplaySubscriptionId(any())(any(), any())).thenReturn(Future.successful(None))

      val userAnswers: UserAnswers = validUserAnswers
        .set(IsThisYourBusinessPage, true)
        .success
        .value

      retrieveUserAnswersData(userAnswers)
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

      val result = route(app, request).value

      status(result) mustEqual OK
      val view       = app.injector.instanceOf[IsThisYourBusinessView]
      val filledForm = form.bind(Map("value" -> "true"))
      contentAsString(result) mustEqual view(filledForm, registrationInfo, NormalMode).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)
      val request =
        FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "return return Internal Server Error for a GET when an error other than NotFoundError returned" in {

      when(mockMatchingService.sendBusinessRegistrationInformation(any())(any(), any()))
        .thenReturn(Future.successful(Left(ServiceUnavailableError)))

      retrieveUserAnswersData(validUserAnswers)
      val request = FakeRequest(GET, routes.IsThisYourBusinessController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }

    "must return Internal Server Error when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)
      val request = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      val view = app.injector.instanceOf[ThereIsAProblemView]

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual view()(request, messages).toString
    }
  }
}
