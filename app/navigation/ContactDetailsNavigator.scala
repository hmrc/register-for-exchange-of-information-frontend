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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ContactDetailsNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case ContactNamePage                    => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode))
    case ContactEmailPage                   => _ => Some(routes.IsContactTelephoneController.onPageLoad(NormalMode))
    case IsContactTelephonePage             => isContactTelephoneRoutes(NormalMode)
    case ContactPhonePage                   => _ => Some(routes.SecondContactController.onPageLoad(NormalMode))
    case IndividualContactEmailPage         => _ => Some(routes.IndividualHaveContactTelephoneController.onPageLoad(NormalMode))
    case IndividualHaveContactTelephonePage => ua => individualHasContactTelephoneRoute(NormalMode)(ua)
    case IndividualContactPhonePage         => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case SecondContactPage                  => isSecondContact(NormalMode)
    case SndContactNamePage                 => _ => Some(routes.SndContactEmailController.onPageLoad(NormalMode))
    case SndContactEmailPage                => _ => Some(routes.SndConHavePhoneController.onPageLoad(NormalMode))
    case SndConHavePhonePage                => haveSecondPhone(NormalMode)
    case SndContactPhonePage                => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case _                                  => _ => None
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case ContactNamePage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          ContactEmailPage,
          routes.ContactEmailController.onPageLoad(CheckMode)
        )

    case ContactEmailPage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          IsContactTelephonePage,
          routes.IsContactTelephoneController.onPageLoad(CheckMode)
        )

    case IsContactTelephonePage => isContactTelephoneRoutes(CheckMode)

    case ContactPhonePage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          SecondContactPage,
          routes.SecondContactController.onPageLoad(CheckMode)
        )
    case IndividualContactEmailPage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          IndividualHaveContactTelephonePage,
          routes.IndividualHaveContactTelephoneController.onPageLoad(CheckMode)
        )

    case IndividualHaveContactTelephonePage => ua => individualHasContactTelephoneRoute(CheckMode)(ua)
    case IndividualContactPhonePage         => _ => Some(routes.CheckYourAnswersController.onPageLoad())

    case SecondContactPage =>
      ua => isSecondContact(CheckMode)(ua)

    case SndContactNamePage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          SndContactEmailPage,
          routes.SndContactEmailController.onPageLoad(CheckMode)
        )
    case SndContactEmailPage =>
      ua =>
        checkNextPageForValueThenRoute(
          CheckMode,
          ua,
          SndConHavePhonePage,
          routes.SndConHavePhoneController.onPageLoad(CheckMode)
        )

    case SndConHavePhonePage => haveSecondPhone(CheckMode)
    case _                   => _ => Some(Navigator.checkYourAnswers)
  }

  private def isContactTelephoneRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(IsContactTelephonePage) map {
      case true => routes.ContactPhoneController.onPageLoad(mode)
      case false =>
        checkNextPageForValueThenRoute(mode, ua, SecondContactPage, routes.SecondContactController.onPageLoad(mode)).get
    }

  private def individualHasContactTelephoneRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(IndividualHaveContactTelephonePage) map {
      case true  => routes.IndividualContactPhoneController.onPageLoad(mode)
      case false => routes.CheckYourAnswersController.onPageLoad()
    }

  private def isSecondContact(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(SecondContactPage) map {
      case true =>
        checkNextPageForValueThenRoute(mode, ua, SndContactNamePage, routes.SndContactNameController.onPageLoad(mode)).get
      case false => routes.CheckYourAnswersController.onPageLoad()
    }

  private def haveSecondPhone(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(SndConHavePhonePage) map {
      case true  => routes.SndContactPhoneController.onPageLoad(mode)
      case false => routes.CheckYourAnswersController.onPageLoad()
    }
}
