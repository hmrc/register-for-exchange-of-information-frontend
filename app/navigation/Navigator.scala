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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case ContactNamePage        => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode))
    case ContactEmailPage       => _ => Some(routes.IsContactTelephoneController.onPageLoad(NormalMode))
    case IsContactTelephonePage => isContactTelephoneRoutes(NormalMode)
    case ContactPhonePage =>
      _ => {
        println("\n\nHEY")
        Some(routes.SecondContactController.onPageLoad(NormalMode))
      }
    case SecondContactPage  => _ => Some(routes.SndContactNameController.onPageLoad(NormalMode))
    case SndContactNamePage => _ => Some(routes.SndContactNameController.onPageLoad(NormalMode))
    case _                  => _ => Some(routes.IndexController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case ContactNamePage        => _ => Some(routes.ContactNameController.onPageLoad(CheckMode))
    case ContactEmailPage       => _ => Some(routes.ContactEmailController.onPageLoad(CheckMode))
    case IsContactTelephonePage => isContactTelephoneRoutes(CheckMode)
    case ContactPhonePage       => _ => Some(routes.SecondContactController.onPageLoad(CheckMode))
    case SecondContactPage      => _ => Some(routes.SndContactNameController.onPageLoad(CheckMode))
    case SndContactNamePage     => _ => Some(routes.SndContactNameController.onPageLoad(CheckMode))
    case _                      => _ => Some(routes.CheckYourAnswersController.onPageLoad())
  }

  private def isContactTelephoneRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(IsContactTelephonePage) map {
      case true  => routes.ContactPhoneController.onPageLoad(mode)
      case false => routes.SecondContactController.onPageLoad(mode)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.SessionExpiredController.onPageLoad()
      }
    case CheckMode =>
      checkRouteMap(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.SessionExpiredController.onPageLoad()
      }
  }
}
