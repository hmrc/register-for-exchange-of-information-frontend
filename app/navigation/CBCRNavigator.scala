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

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class CBCRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case ContactNamePage        => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode))
    case ContactEmailPage       => _ => Some(routes.IsContactTelephoneController.onPageLoad(NormalMode))
    case IsContactTelephonePage => isContactTelephoneRoutes(NormalMode)
    case ContactPhonePage       => _ => Some(routes.SecondContactController.onPageLoad(NormalMode))
    case SecondContactPage      => isSecondContact(NormalMode)
    case SndContactNamePage     => _ => Some(routes.SndContactEmailController.onPageLoad(NormalMode))
    case SndContactEmailPage    => _ => Some(routes.SndConHavePhoneController.onPageLoad(NormalMode))
    case SndConHavePhonePage    => haveSecondPhone(NormalMode)
    case SndContactPhonePage    => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case _                      => _ => Some(routes.IndexController.onPageLoad())
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case IsContactTelephonePage => isContactTelephoneRoutes(CheckMode)
    case SecondContactPage      => isSecondContact(CheckMode)
    case SndConHavePhonePage    => haveSecondPhone(CheckMode)
    case _                      => _ => Some(Navigator.checkYourAnswers)
  }

  private def isContactTelephoneRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(IsContactTelephonePage) map {
      case true                       => routes.ContactPhoneController.onPageLoad(mode)
      case false if mode == CheckMode => routes.CheckYourAnswersController.onPageLoad()
      case false                      => routes.SecondContactController.onPageLoad(mode)
    }

  private def isSecondContact(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(SecondContactPage) map {
      // optimization works only for last sub-journey
      case true if mode == CheckMode => routes.SndContactNameController.onPageLoad(NormalMode)
      case true                      => routes.SndContactNameController.onPageLoad(mode)
      case false                     => routes.CheckYourAnswersController.onPageLoad()
    }

  private def haveSecondPhone(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(SndConHavePhonePage) map {
      case true  => routes.SndContactPhoneController.onPageLoad(mode)
      case false => routes.CheckYourAnswersController.onPageLoad()
    }
}