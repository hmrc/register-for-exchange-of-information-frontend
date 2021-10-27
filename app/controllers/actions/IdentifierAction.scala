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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import models.Regime
import models.requests.IdentifierRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, credentialRole}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction {
  def apply(regime: Regime): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]
  //def apply(): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]
}
//trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def apply(regime: Regime): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] =
    new AuthenticatedIdentifierActionWithRegime(authConnector, config, parser, regime)

//  override def apply(): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] =
//    new AuthenticatedIdentifierActionImpl(authConnector, config, parser)
}

class AuthenticatedIdentifierActionWithRegime @Inject() (
  val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default,
  regime: Regime
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]
    with AuthorisedFunctions {
  private val logger: Logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and affinityGroup and credentialRole) {
        case _ ~ _ ~ Some(Agent) ~ _ =>
          Future.successful(Redirect(controllers.routes.UnauthorisedAgentController.onPageLoad(regime)))
        case _ ~ _ ~ _ ~ Some(Assistant) =>
          Future.successful(Redirect(controllers.routes.UnauthorisedAssistantController.onPageLoad(regime)))
        case _ ~ enrolments ~ _ ~ _ if enrolments.enrolments.exists(_.key == config.enrolmentKey(regime.toString)) =>
          logger.info("MDR enrolment exists")
          Future.successful(Redirect(config.mandatoryDisclosureRulesFrontendUrl))
        case Some(internalID) ~ _ ~ _ ~ _ => block(IdentifierRequest(request, internalID))
        case _                            => throw new UnauthorizedException("Unable to retrieve internal Id")
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
        case _: InsufficientEnrolments => Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad(regime))
        case _: AuthorisationException =>
          Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad(regime))
      }
  }
}

class AuthenticatedIdentifierActionImpl @Inject() (
  val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]
    with AuthorisedFunctions {
  private val logger: Logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and affinityGroup and credentialRole) {
        case Some(internalID) ~ _ ~ _ ~ _ => block(IdentifierRequest(request, internalID))
        case _                            => throw new UnauthorizedException("Unable to retrieve internal Id")
      }

  }
}
