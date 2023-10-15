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
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError}
import models.matching.OrgRegistrationInfo
import models.register.response.details.AddressResponse
import models.requests.DataRequest
import models.{SubscriptionID, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import services.{EmailService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ControllerHelperSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  val mockTaxEnrolmentService: TaxEnrolmentService = mock[TaxEnrolmentService]
  val mockEmailService: EmailService               = mock[EmailService]

  override def beforeEach(): Unit =
    reset(mockEmailService, mockTaxEnrolmentService, mockEmailService)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentService),
        bind[EmailService].toInstance(mockEmailService)
      )

  val controller: ControllerHelper = app.injector.instanceOf[ControllerHelper]

  val subscriptionId: SubscriptionID = SubscriptionID("ABC123")

  val userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(DoYouHaveUniqueTaxPayerReferencePage, false)
    .success
    .value
    .set(ContactNamePage, "")
    .success
    .value
    .set(ContactEmailPage, "test@test.com")
    .success
    .value

  "ControllerHelper" - {
    "updateSubscriptionIdAndCreateEnrolment update the subscription ID in user answers and create an enrolment" in {

      val affinityGroup: AffinityGroup         = AffinityGroup.Individual
      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(1)))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.RegistrationConfirmationController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(1)).sendAnLogEmail(any(), any())(any())
    }
    "Redirect to Individual already registered when tax enrolments returns EnrolmentExists error" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Individual

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.IndividualAlreadyRegisteredController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(0)).sendAnLogEmail(any(), any())(any())
    }
    "Redirect to Business already registered with ID when tax enrolments returns EnrolmentExists error" in {
      val affinityGroup: AffinityGroup         = AffinityGroup.Organisation
      val addressResponse                      = AddressResponse("line1", None, None, None, None, "UK")
      val userAnswers2                         = userAnswers.set(RegistrationInfoPage, OrgRegistrationInfo(safeId, name = "", address = addressResponse)).success.value
      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers2)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.BusinessAlreadyRegisteredController.onPageLoadWithId().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(0)).sendAnLogEmail(any(), any())(any())
    }
    "Redirect to Business already registered without ID when tax enrolments returns EnrolmentExists error" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(EnrolmentExistsError(mock[GroupIds]))))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.BusinessAlreadyRegisteredController.onPageLoadWithoutId().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(0)).sendAnLogEmail(any(), any())(any())
    }
    "Redirect to SomeInformation is missing controller" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(MandatoryInformationMissingError("Error"))))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SomeInformationIsMissingController.onPageLoad().url)

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(0)).sendAnLogEmail(any(), any())(any())
    }
    "Return service unavailable for other errors" in {
      val affinityGroup: AffinityGroup = AffinityGroup.Organisation

      val dataRequest: DataRequest[AnyContent] = DataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)

      when(mockTaxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ApiError.ServiceUnavailableError)))
      when(mockEmailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

      status(result) shouldBe SERVICE_UNAVAILABLE

      verify(mockTaxEnrolmentService, times(1)).checkAndCreateEnrolment(any(), any(), any())(any(), any())
      verify(mockEmailService, times(0)).sendAnLogEmail(any(), any())(any())
    }
  }
}
