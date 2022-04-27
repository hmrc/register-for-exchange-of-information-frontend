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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{CBC, MDR, Regime}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, credentialRole}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction {
  def apply(regime: Regime): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]
}

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def apply(regime: Regime): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] =
    new AuthenticatedIdentifierActionWithRegime(authConnector, config, parser, regime)
}

class AuthenticatedIdentifierActionWithRegime @Inject() (
  val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default,
  regime: Regime
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]
    with AuthorisedFunctions
    with Logging {

  val enrolmentKey: String = config.enrolmentKey(regime.toString)

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and affinityGroup and credentialRole) {
        case _ ~ _ ~ Some(Agent) ~ _ =>
          Future.successful(Redirect(controllers.routes.UnauthorisedAgentController.onPageLoad(regime)))
        case _ ~ enrolments ~ _ ~ Some(Assistant) if enrolments.enrolments.exists(_.key == enrolmentKey) =>
          regime match {
            case MDR => Future.successful(Redirect(config.mandatoryDisclosureRulesFrontendUrl))
            case CBC => Future.successful(NotImplemented("Not Implemented")) //TODO: Change this to redirect to CBC
          }
        case _ ~ _ ~ _ ~ Some(Assistant) =>
          Future.successful(Redirect(controllers.routes.UnauthorisedAssistantController.onPageLoad(regime)))
        case Some(internalID) ~ enrolments ~ Some(Individual) ~ _ if regime == CBC =>
          Future.successful(
            NotImplemented("Not Implimented - covered by DAC6-1632")
          ) //TODO: Change this to new Individual CBC kick out page as part of DAC6-1632
        case Some(internalID) ~ enrolments ~ Some(affinityGroup) ~ _ => block(IdentifierRequest(request, internalID, affinityGroup, enrolments.enrolments))
        case _                                                       => throw new UnauthorizedException("Unable to retrieve internal Id")
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl(regime))))
        case _: AuthorisationException =>
          Redirect(controllers.routes.ThereIsAProblemController.onPageLoad(regime))
      }
  }
}
