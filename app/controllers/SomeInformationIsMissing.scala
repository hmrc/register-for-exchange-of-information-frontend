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

import models.{BusinessType, Regime}
import models.requests.DataRequest
import navigation.Navigator
import pages.{BusinessTypePage, ContactNamePage, SndContactNamePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}

import scala.concurrent.Future

object SomeInformationIsMissing {

  val missingInformationResult: Regime => Future[Result] = regime => Future.successful(Redirect(Navigator.missingInformation(regime)))

  def isMissingContactName(regime: Regime)(f: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers
      .get(ContactNamePage)
      .fold(missingInformationResult(regime)) {
        name => f(name)
      }

  def isMissingSecondContactName(regime: Regime)(f: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers
      .get(SndContactNamePage)
      .fold(missingInformationResult(regime)) {
        name => f(name)
      }

  def isMissingBusinessType(regime: Regime)(f: BusinessType => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers
      .get(BusinessTypePage)
      .fold(missingInformationResult(regime)) {
        name => f(name)
      }
}
