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

import config.FrontendAppConfig
import controllers.actions._
import pages.SubscriptionIDPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{RegistrationConfirmationView, ThereIsAProblemView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  val appConfig: FrontendAppConfig,
  standardActionSets: StandardActionSets,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: RegistrationConfirmationView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    standardActionSets.identifiedWithoutEnrolmentCheck().async { implicit request =>
      request.userAnswers.get(SubscriptionIDPage) match {
        case Some(id) =>
          sessionRepository.reset(request.userId) map { _ =>
            Ok(view(id.value))
          }
        case None     =>
          logger.warn("SubscriptionIDPage: Subscription Id is missing")
          Future.successful(InternalServerError(errorView()))
      }
    }
}
