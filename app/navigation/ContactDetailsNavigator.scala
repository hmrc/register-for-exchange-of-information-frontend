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
import models.BusinessType.Sole
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ContactDetailsNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => Regime => UserAnswers => Option[Call] = {
    case ContactNamePage        => regime => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode, regime))
    case ContactEmailPage       => regime => _ => Some(routes.IsContactTelephoneController.onPageLoad(NormalMode, regime))
    case IsContactTelephonePage => regime => isContactTelephoneRoutes(NormalMode)(regime)
    case ContactPhonePage       => regime => contactTelephoneNumber(NormalMode)(regime)
    case SecondContactPage      => regime => isSecondContact(NormalMode)(regime)
    case SndContactNamePage     => regime => _ => Some(routes.SndContactEmailController.onPageLoad(NormalMode, regime))
    case SndContactEmailPage    => regime => _ => Some(routes.SndConHavePhoneController.onPageLoad(NormalMode, regime))
    case SndConHavePhonePage    => regime => haveSecondPhone(NormalMode)(regime)
    case SndContactPhonePage    => regime => _ => Some(routes.CheckYourAnswersController.onPageLoad(regime))
    case _                      => _ => _ => None
  }

  override val checkRouteMap: Page => Regime => UserAnswers => Option[Call] = {
    case ContactNamePage        => regime => _ => Some(routes.ContactEmailController.onPageLoad(CheckMode, regime))
    case ContactEmailPage       => regime => _ => Some(routes.IsContactTelephoneController.onPageLoad(CheckMode, regime))
    case IsContactTelephonePage => isContactTelephoneRoutes(CheckMode)
    case ContactPhonePage       => regime => contactTelephoneNumber(CheckMode)(regime)
    case SecondContactPage      => isSecondContact(CheckMode)
    case SndContactNamePage     => regime => _ => Some(routes.SndContactEmailController.onPageLoad(CheckMode, regime))
    case SndContactEmailPage    => regime => _ => Some(routes.SndConHavePhoneController.onPageLoad(CheckMode, regime))
    case SndConHavePhonePage    => haveSecondPhone(CheckMode)
    case _                      => regime => _ => Some(Navigator.checkYourAnswers(regime))
  }

  private def contactTelephoneNumber(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) match {
      case Some(true) =>
        ua.get(BusinessTypePage) map {
          case Sole => routes.CheckYourAnswersController.onPageLoad(regime)
          case _    => routes.SecondContactController.onPageLoad(mode, regime)
        }
      case Some(false) =>
        ua.get(WhatAreYouRegisteringAsPage) map {
          case RegistrationTypeIndividual => routes.CheckYourAnswersController.onPageLoad(regime)
          case RegistrationTypeBusiness   => routes.SecondContactController.onPageLoad(mode, regime)
        }
      case None => Some(routes.SecondContactController.onPageLoad(mode, regime))
    }

  private def isContactTelephoneRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(IsContactTelephonePage) map {
      case true => routes.ContactPhoneController.onPageLoad(mode, regime)
//      case false if mode == CheckMode => routes.CheckYourAnswersController.onPageLoad(regime)
      case false =>
        if (isIndividual(ua)) {
          routes.CheckYourAnswersController.onPageLoad(regime)
        } else {
          routes.SecondContactController.onPageLoad(mode, regime)
        }
    }

  private def isSecondContact(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(SecondContactPage) map {
      // optimization works only for last sub-journey
//      case true if mode == CheckMode => routes.SndContactNameController.onPageLoad(CheckMode, regime)
      case true  => routes.SndContactNameController.onPageLoad(mode, regime)
      case false => routes.CheckYourAnswersController.onPageLoad(regime)
    }

  private def haveSecondPhone(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(SndConHavePhonePage) map {
      case true  => routes.SndContactPhoneController.onPageLoad(mode, regime)
      case false => routes.CheckYourAnswersController.onPageLoad(regime)
    }

  private def isIndividual(ua: UserAnswers): Boolean = ua.get(DoYouHaveUniqueTaxPayerReferencePage) match {
    case Some(true) =>
      ua.get(BusinessTypePage) match {
        case Some(Sole) => true
        case _          => false
      }
    case Some(false) =>
      ua.get(WhatAreYouRegisteringAsPage) match {
        case Some(RegistrationTypeIndividual) => true
        case Some(RegistrationTypeBusiness)   => false
        case _                                => false
      }
    case None => false
  }
}
