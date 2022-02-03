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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ContactDetailsNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => Regime => UserAnswers => Option[Call] = {
    case ContactNamePage                    => regime => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode, regime))
    case ContactEmailPage                   => regime => _ => Some(routes.IsContactTelephoneController.onPageLoad(NormalMode, regime))
    case IsContactTelephonePage             => regime => isContactTelephoneRoutes(NormalMode)(regime)
    case ContactPhonePage                   => regime => _ => Some(routes.SecondContactController.onPageLoad(NormalMode, regime))
    case IndividualContactEmailPage         => regime => _ => Some(routes.IndividualHaveContactTelephoneController.onPageLoad(NormalMode, regime))
    case IndividualHaveContactTelephonePage => regime => ua => individualHasContactTelephoneRoute(NormalMode)(regime)(ua)
    case IndividualContactPhonePage         => regime => _ => Some(routes.CheckYourAnswersController.onPageLoad(regime))
    case SecondContactPage                  => regime => isSecondContact(NormalMode)(regime)
    case SndContactNamePage                 => regime => _ => Some(routes.SndContactEmailController.onPageLoad(NormalMode, regime))
    case SndContactEmailPage                => regime => _ => Some(routes.SndConHavePhoneController.onPageLoad(NormalMode, regime))
    case SndConHavePhonePage                => regime => haveSecondPhone(NormalMode)(regime)
    case SndContactPhonePage                => regime => _ => Some(routes.CheckYourAnswersController.onPageLoad(regime))
    case _                                  => _ => _ => None
  }

  override val checkRouteMap: Page => Regime => UserAnswers => Option[Call] = {
    case ContactNamePage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            ContactEmailPage,
            routes.ContactEmailController.onPageLoad(CheckMode, regime)
          )

    case ContactEmailPage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            IsContactTelephonePage,
            routes.IsContactTelephoneController.onPageLoad(CheckMode, regime)
          )

    case IsContactTelephonePage => isContactTelephoneRoutes(CheckMode)

    case ContactPhonePage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            SecondContactPage,
            routes.SecondContactController.onPageLoad(CheckMode, regime)
          )
    case IndividualContactEmailPage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            IndividualHaveContactTelephonePage,
            routes.IndividualHaveContactTelephoneController.onPageLoad(CheckMode, regime)
          )

    case IndividualHaveContactTelephonePage => regime => ua => individualHasContactTelephoneRoute(CheckMode)(regime)(ua)
    case IndividualContactPhonePage         => regime => _ => Some(routes.CheckYourAnswersController.onPageLoad(regime))

    case SecondContactPage =>
      regime => ua => isSecondContact(CheckMode)(regime)(ua)

    case SndContactNamePage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            SndContactEmailPage,
            routes.SndContactEmailController.onPageLoad(CheckMode, regime)
          )
    case SndContactEmailPage =>
      regime =>
        ua =>
          checkNextPageForValueThenRoute(
            CheckMode,
            regime,
            ua,
            SndConHavePhonePage,
            routes.SndConHavePhoneController.onPageLoad(CheckMode, regime)
          )

    case SndConHavePhonePage => haveSecondPhone(CheckMode)
    case _                   => regime => _ => Some(Navigator.checkYourAnswers(regime))
  }

  private def isContactTelephoneRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(IsContactTelephonePage) map {
      case true => routes.ContactPhoneController.onPageLoad(mode, regime)
      case false =>
        checkNextPageForValueThenRoute(mode, regime, ua, SecondContactPage, routes.SecondContactController.onPageLoad(mode, regime)).get
    }

  private def individualHasContactTelephoneRoute(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(IndividualHaveContactTelephonePage) map {
      case true  => routes.IndividualContactPhoneController.onPageLoad(mode, regime)
      case false => routes.CheckYourAnswersController.onPageLoad(regime)
    }

  private def isSecondContact(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(SecondContactPage) map {
      case true =>
        checkNextPageForValueThenRoute(mode, regime, ua, SndContactNamePage, routes.SndContactNameController.onPageLoad(mode, regime)).get
      case false => routes.CheckYourAnswersController.onPageLoad(regime)
    }

  private def haveSecondPhone(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(SndConHavePhonePage) map {
      case true  => routes.SndContactPhoneController.onPageLoad(mode, regime)
      case false => routes.CheckYourAnswersController.onPageLoad(regime)
    }
}
