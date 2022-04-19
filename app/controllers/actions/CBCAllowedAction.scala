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

import config.FrontendAppConfig
import models.requests.IdentifierRequest
import models.{CBC, MDR, Regime}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CBCAllowedActionWithRegime @Inject() (
  regime: Regime,
  appConfig: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends ActionFilter[IdentifierRequest]
    with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    logger.warn("Entering filter")
    regime match {
      case CBC if appConfig.allowCBCregistration =>
        logger.info("Allow CBC")
        Future.successful(None)
      case CBC if !appConfig.allowCBCregistration =>
        logger.info("CBC registration is disabled")
        Future.successful(Some(Redirect(controllers.routes.ThereIsAProblemController.onPageLoad(regime))))
      case MDR => Future.successful(None)
      case _ =>
        logger.info(s"Unrecognised regime ${regime.toString}")
        Future.successful(Some(Redirect(controllers.routes.ThereIsAProblemController.onPageLoad(regime))))
    }
  }
}

class CBCAllowedAction @Inject() (appConfig: FrontendAppConfig)(implicit val executionContext: ExecutionContext) {
  def apply[T](regime: Regime): ActionFilter[IdentifierRequest] = new CBCAllowedActionWithRegime(regime, appConfig)
}
