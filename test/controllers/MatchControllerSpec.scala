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
import matchers.JsonMatchers
import models.{CheckMode, MDR, Name, NormalMode, UserAnswers}
import models.matching.MatchingType.AsIndividual
import models.matching.RegistrationInfo
import org.mockito.ArgumentMatchers.any
import pages.{RegistrationInfoPage, WhatIsYourDateOfBirthPage, WhatIsYourNamePage, WhatIsYourNationalInsuranceNumberPage}
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty, GET}
import play.twirl.api.Html
import services.BusinessMatchingService
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.viewmodels.NunjucksSupport

import java.time.LocalDate
import scala.concurrent.Future

class MatchControllerSpec extends SpecBase with ControllerMockFixtures with NunjucksSupport with JsonMatchers {

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

  "MatchControllerSpec" - {

    "onIndividualMatch" - {

      //val registrationInfo = RegistrationInfo.build("safeId", AsIndividual)

      val name             = Name("firstname", "lastname")
      val nino             = Nino("AA000000A")
      val dob              = LocalDate.now
      val registrationInfo = RegistrationInfo.build(name, nino, Option(dob))

      val validUserAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(WhatIsYourNationalInsuranceNumberPage, nino)
        .success
        .value
        .set(WhatIsYourNamePage, name)
        .success
        .value
        .set(WhatIsYourDateOfBirthPage, dob)
        .success
        .value
        .set(RegistrationInfoPage, registrationInfo)
        //.set(RegistrationInfoPage, RegistrationInfo.build(Name("dupa", "blah"), Nino("AA000000B"), Option(dob)))
        .success
        .value

      // todo WeHaveConfirmedYourIdentityController
      "redirect to WeHaveConfirmedYourIdentityController when there is a match" in {
        when(mockMatchingService.sendIndividualRegistrationInformation(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(registrationInfo)))

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val newUA = validUserAnswers
          .set(RegistrationInfoPage, RegistrationInfo.build(Name("dupa", "blah"), Nino("AA000000B"), Option(dob)))
          .success
          .value
        //retrieveUserAnswersData(validUserAnswers)
        retrieveUserAnswersData(newUA)
        val request = FakeRequest(GET, routes.MatchController.onIndividualMatch(NormalMode, MDR).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.WeHaveConfirmedYourIdentityController.onPageLoad(MDR).url
      }

      // todo CheckYourAnswersController
      "redirect to CheckYourAnswers if duplicate submission" in {
        when(mockMatchingService.sendIndividualRegistrationInformation(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(registrationInfo)))

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        retrieveUserAnswersData(validUserAnswers)
        val request = FakeRequest(GET, routes.MatchController.onIndividualMatch(CheckMode, MDR).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad(MDR).url
      }

      // todo WeCouldNotConfirmController
      "redirectd to WeCouldNotConfirmController if duplicate submission" in {
        when(mockMatchingService.sendIndividualRegistrationInformation(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(registrationInfo)))

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        retrieveUserAnswersData(validUserAnswers)
        val request = FakeRequest(GET, routes.MatchController.onIndividualMatch(NormalMode, MDR).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.WeCouldNotConfirmController.onPageLoad("identity", MDR).url
      }
    }
  }
}
