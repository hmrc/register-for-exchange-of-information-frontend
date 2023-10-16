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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.IdentifierType
import models.requests.IdentifierRequest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.mvc.Results.{Ok, Redirect}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier}

import scala.concurrent.Future

class CtUtrRetrievalActionSpec extends SpecBase with TableDrivenPropertyChecks {

  private val fakeRequest = FakeRequest("", "")

  private def block[A]: IdentifierRequest[A] => Future[Result] = (_: IdentifierRequest[A]) => {
    Future.successful(Ok)
  }

  private val applicationBuilder       = new GuiceApplicationBuilder()
  private val action                   = applicationBuilder.injector.instanceOf[CtUtrRetrievalActionProvider]
  private val config                   = applicationBuilder.injector.instanceOf[FrontendAppConfig]
  private val ctUtrEnrolmentIdentifier = EnrolmentIdentifier(IdentifierType.UTR, utr.uniqueTaxPayerReference)

  "CT UTR Retrieval Action" - {

    "when the affinity group is Organisation" - {

      val affinityGroup = AffinityGroup.Organisation

      "must execute request block when CT-UTR enrolment exists" in {
        val ctUtrEnrolment    = Enrolment(config.ctEnrolmentKey, Seq(ctUtrEnrolmentIdentifier), state = "")
        val identifierRequest = IdentifierRequest(fakeRequest, UserAnswersId, affinityGroup, enrolments = Set(ctUtrEnrolment))

        val result = action.invokeBlock(identifierRequest, block)

        result.futureValue mustBe Ok
      }

      "must execute request block when CT-UTR enrolment does not exists" in {
        val identifierRequest = IdentifierRequest(fakeRequest, UserAnswersId, affinityGroup, enrolments = Set.empty)

        val result = action.invokeBlock(identifierRequest, block)

        result.futureValue mustBe Ok
      }
    }

    val nonOrganisationAffinityGroups = Table(
      "affinityGroup",
      AffinityGroup.Individual,
      AffinityGroup.Agent
    )

    forAll(nonOrganisationAffinityGroups) {
      affinityGroup =>
        s"when the affinity group is $affinityGroup" - {

          "must redirect to ThereIsAProblemPage when CT-UTR enrolment exists" in {
            val ctUtrEnrolment    = Enrolment(config.ctEnrolmentKey, Seq(ctUtrEnrolmentIdentifier), state = "")
            val identifierRequest = IdentifierRequest(fakeRequest, UserAnswersId, affinityGroup, enrolments = Set(ctUtrEnrolment))

            val result = action.invokeBlock(identifierRequest, block)

            result.futureValue mustBe Redirect(controllers.routes.ThereIsAProblemController.onPageLoad())
          }

          "must execute request block when CT-UTR enrolment does not exists" in {
            val identifierRequest = IdentifierRequest(fakeRequest, UserAnswersId, affinityGroup, enrolments = Set.empty)

            val result = action.invokeBlock(identifierRequest, block)

            result.futureValue mustBe Ok
          }
        }
    }

  }

}
