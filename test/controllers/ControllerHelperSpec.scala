package controllers

import base.{ControllerMockFixtures, SpecBase}
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import models.matching.SafeId
import models.requests.{DataRequest, OptionalDataRequest}
import uk.gov.hmrc.auth.core.AffinityGroup
import models.{SubscriptionID, UserAnswers}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.sird.?
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ThereIsAProblemView

//class ControllerHelperSpec extends SpecBase {}
import org.mockito.ArgumentMatchers.any
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{EmailService, TaxEnrolmentService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ControllerHelperSpec extends SpecBase with ControllerMockFixtures{

  val taxEnrolmentService = mock[TaxEnrolmentService]
  val errorView = mock[ThereIsAProblemView]
  val sessionRepository = mock[SessionRepository]
  val emailService = mock[EmailService]
  val affinityGroup: AffinityGroup = AffinityGroup.Individual

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[TaxEnrolmentService].toInstance(taxEnrolmentService),
        bind[ThereIsAProblemView].toInstance(errorView),
        bind[SessionRepository].toInstance(sessionRepository),
        bind[EmailService].toInstance(emailService)
      )

  val controller = new ControllerHelper(
    controllerComponents = stubMessagesControllerComponents(),
    taxEnrolmentService = taxEnrolmentService,
    errorView = errorView,
    sessionRepository = sessionRepository,
    emailService = emailService
  )

  val fakeRequest = FakeRequest("", "")

  "updateSubscriptionIdAndCreateEnrolment update the subscription ID in user answers and create an enrolment" in {
    val safeId = SafeId("123456789")
    val subscriptionId = SubscriptionID("ABC123")
    val userAnswers = UserAnswers(userAnswersId)
      .set(DoYouHaveUniqueTaxPayerReferencePage, false)
      .success
      .value
      .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
      .success
      .value
      .set(ContactNamePage, "")
      .success
      .value
      .set(ContactEmailPage, "test@test.com")
      .success
      .value
    val dataRequest = OptionalDataRequest(fakeRequest, userAnswersId, affinityGroup, userAnswers)



    when(taxEnrolmentService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Right(1)))
    when(emailService.sendAnLogEmail(any(), any())(any())).thenReturn(Future.successful(200))
    when(sessionRepository.set(any())).thenReturn(Future.successful(true))

    val result: Future[Result] = controller.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionId)(HeaderCarrier(), dataRequest)

    status(result) shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some(routes.RegistrationConfirmationController.onPageLoad().url)

    val updatedAnswers = userAnswers.set(SubscriptionIDPage, subscriptionId).getOrElse(fail("Failed to set SubscriptionIDPage in user answers"))
    verify(sessionRepository).set(updatedAnswers)
    verify(taxEnrolmentService).checkAndCreateEnrolment(safeId, userAnswers, subscriptionId)
    verify(emailService).sendAnLogEmail(userAnswers, subscriptionId)
  }

}

