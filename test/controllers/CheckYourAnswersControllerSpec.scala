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
import connectors.AddressLookupConnector
import controllers.actions._
import models.enrolment.GroupIds
import models.error.ApiError._
import models.matching.{IndRegistrationInfo, SafeId}
import models.{Address, Country, SubscriptionID, UserAnswers}
import navigation.MDRNavigator
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{BusinessMatchingWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import views.html.ThereIsAProblemView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ControllerMockFixtures with BeforeAndAfterEach {

  final val mockRegistrationService: BusinessMatchingWithoutIdService = mock[BusinessMatchingWithoutIdService]

  lazy val loadRoute   = routes.CheckYourAnswersController.onPageLoad().url
  lazy val submitRoute = routes.CheckYourAnswersController.onSubmit().url

  // first contact
  val firstContactName    = "first-contact-name"
  val firstContactEmail   = "first-contact-email"
  val isFirstContactPhone = true
  val firstContactPhone   = "+44 0808 157 0192"

  // second contact
  val isSecondContact                               = true
  val secondContactName                             = "second-contact-name"
  val secondContactEmail                            = "second-contact-email"
  val isSecondContactPhone                          = true
  val secondContactPhone                            = "+44 0808 157 0193"
  val mockSubscriptionService: SubscriptionService  = mock[SubscriptionService]
  val mockTaxEnrolmentsService: TaxEnrolmentService = mock[TaxEnrolmentService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
        bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService)
      )

  override def beforeEach(): Unit = {
    reset(mockSubscriptionService, mockRegistrationService, mockTaxEnrolmentsService)
    super.beforeEach
  }

  val address: Address = Address("line 1", Some("line 2"), "line 3", Some("line 4"), Some(""), Country.GB)

  "CheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET - First Contact with phone" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result).contains(firstContactName) mustBe true
        contentAsString(result).contains(firstContactEmail) mustBe true
        contentAsString(result).contains(firstContactPhone) mustBe true

      }

      "must return OK and the correct view for a GET - First Contact without phone" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result).contains(firstContactName) mustBe true
        contentAsString(result).contains(firstContactEmail) mustBe true
        contentAsString(result).contains(firstContactPhone) mustBe false
      }

      "must return OK and the correct view for a GET - Without Second Contact" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value
          .set(SecondContactPage, !isSecondContact)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result).contains(firstContactName) mustBe true
        contentAsString(result).contains(firstContactEmail) mustBe true
        contentAsString(result).contains(secondContactName) mustBe false
      }

      "must return OK and the correct view for a GET - With Second Contact with phone" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactEmailPage, "test@test.com")
          .success
          .value
          .set(ContactNamePage, "Name Name")
          .success
          .value
          .set(ContactHavePhonePage, false)
          .success
          .value
          .set(SecondContactPage, true)
          .success
          .value
          .set(SndContactNamePage, secondContactName)
          .success
          .value
          .set(SndContactEmailPage, secondContactEmail)
          .success
          .value
          .set(SndConHavePhonePage, true)
          .success
          .value
          .set(SndContactPhonePage, secondContactPhone)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result).contains("Name Name") mustBe true
        contentAsString(result).contains("test@test.com") mustBe true
        contentAsString(result).contains(secondContactName) mustBe true
        contentAsString(result).contains(secondContactEmail) mustBe true
        contentAsString(result).contains(secondContactPhone) mustBe true

      }

      "must return OK and the correct view for a GET - With Second Contact without phone" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ContactNamePage, firstContactName)
          .success
          .value
          .set(ContactEmailPage, firstContactEmail)
          .success
          .value
          .set(ContactPhonePage, firstContactPhone)
          .success
          .value
          .set(SecondContactPage, isSecondContact)
          .success
          .value
          .set(SndContactNamePage, secondContactName)
          .success
          .value
          .set(SndContactEmailPage, secondContactEmail)
          .success
          .value
          .set(SndConHavePhonePage, false)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result).contains(firstContactName)
        contentAsString(result).contains(firstContactEmail)
        contentAsString(result).mustNot(contain(secondContactName))

        contentAsString(result).contains(firstContactName) mustBe true
        contentAsString(result).contains(firstContactEmail) mustBe true
        contentAsString(result).contains(firstContactPhone) mustBe true
        contentAsString(result).contains(secondContactName) mustBe true
        contentAsString(result).contains(secondContactEmail) mustBe true
        contentAsString(result).contains(secondContactPhone) mustBe false
      }

    }

    "onSubmit" - {

      "must redirect to 'confirmation' page for 'Individual with Id' journey" in {

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
      }

      "must redirect to 'confirmation' page for 'Business with Id' journey" in {

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
      }

      "must redirect to 'Technical difficulty' page for 'Business with Id' journey when tax enrolment fails" in {

        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEnrolmentError)))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any())).thenReturn(Future.successful(Right(SubscriptionID("id"))))
        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }

      "must redirect to 'confirmation' page for 'Individual without Id' journey" in {

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, false)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
      }

      "must redirect to 'confirmation' page for 'Business without Id' journey" in {

        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(NO_CONTENT)))
        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, false)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad().url
      }

      "must redirect to 'JourneyRecovery' page when some information missing" in {

        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Left(MandatoryInformationMissingError())))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SomeInformationIsMissingController.onPageLoad().url
      }

      "must redirect to 'Individual already registered' page when there is EnrolmentExistsError and Affinity Group is Individual" in {

        val app: Application =
          new GuiceApplicationBuilder()
            .overrides(
              bind[SubscriptionService].toInstance(mockSubscriptionService),
              bind[BusinessMatchingWithoutIdService].toInstance(mockRegistrationService),
              bind[TaxEnrolmentService].toInstance(mockTaxEnrolmentsService),
              bind[DataRequiredAction].to[DataRequiredActionImpl],
              bind[DataRetrievalAction].toInstance(mockDataRetrievalAction),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[MDRNavigator].toInstance(mdrFakeNavigator),
              bind[AddressLookupConnector].toInstance(mockAddressLookupConnector),
              bind[IdentifierAction].toInstance(new FakeIdentifierAction(injectedParsers) {
                override val affinityGroup: AffinityGroup = Individual
              })
            )
            .build()

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq("id"), Seq.empty)))))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.IndividualAlreadyRegisteredController.onPageLoad().url
      }

      "must redirect to 'Business already registered' page when there is EnrolmentExistsError" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq("id"), Seq.empty)))))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.BusinessAlreadyRegisteredController.onPageLoadWithoutId().url
      }

      "must redirect to 'Business already registered' page when there is EnrolmentExistsError in withId flow" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(SubscriptionID("id"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockTaxEnrolmentsService.checkAndCreateEnrolment(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(EnrolmentExistsError(GroupIds(Seq("id"), Seq.empty)))))

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(RegistrationInfoPage, IndRegistrationInfo(safeId))
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.BusinessAlreadyRegisteredController.onPageLoadWithId().url
      }

      "must render 'thereIsAProblem' page when 'createSubscription' fails with UnableToCreateEMTPSubscriptionError" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEMTPSubscriptionError)))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        val view = app.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages).toString
      }

      "must render 'thereIsAProblem' page when 'createSubscription' fails with ServiceUnavailableError" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(ServiceUnavailableError)))
        when(mockRegistrationService.registerWithoutId()(any(), any()))
          .thenReturn(Future.successful(Right(SafeId("SAFEID"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers("Id")
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(DoYouHaveNINPage, true)
          .success
          .value

        retrieveUserAnswersData(userAnswers)

        val request = FakeRequest(POST, submitRoute)

        val result = route(app, request).value

        val view = app.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual SERVICE_UNAVAILABLE
        contentAsString(result) mustEqual view()(request, messages).toString
      }

      "must go to the Journey recovery controller and the correct view for a POST - if both individual and organisation are not present" in {

        when(mockSubscriptionService.checkAndCreateSubscription(any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnableToCreateEMTPSubscriptionError)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockRegistrationService.registerWithoutId()(any(), any())).thenReturn(Future.successful(Left(MandatoryInformationMissingError())))

        retrieveUserAnswersData(emptyUserAnswers)
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SomeInformationIsMissingController.onPageLoad().url
      }
    }
  }
}
