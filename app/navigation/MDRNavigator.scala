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
class MDRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => Regime => UserAnswers => Option[Call] = {
    case DoYouHaveUniqueTaxPayerReferencePage  => regime => doYouHaveUniqueTaxPayerReference(NormalMode)(regime)
    case WhatAreYouRegisteringAsPage           => regime => whatAreYouRegisteringAs(NormalMode)(regime)
    case DoYouHaveNINPage                      => regime => doYouHaveNINORoutes(NormalMode)(regime)
    case WhatIsYourNationalInsuranceNumberPage => regime => _ => Some(routes.WhatIsYourNameController.onPageLoad(NormalMode, regime))
    case WhatIsYourNamePage                    => regime => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode, regime))
    case WhatIsYourDateOfBirthPage             => whatIsYourDateOfBirthRoutes(NormalMode)
    case BusinessWithoutIDNamePage             => regime => _ => Some(routes.BusinessHaveDifferentNameController.onPageLoad(NormalMode, regime))
    case BusinessHaveDifferentNamePage         => regime => businessHaveDifferentNameRoutes(NormalMode)(regime)
    case WhatIsTradingNamePage                 => regime => _ => Some(routes.AddressWithoutIdController.onPageLoad(NormalMode, regime))
    case NonUkNamePage                         => regime => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode, regime))
    case DoYouLiveInTheUKPage                  => regime => doYouLiveInTheUkRoutes(NormalMode)(regime)
    case AddressUKPage                         => regime => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode, regime))
    case AddressWithoutIdPage                  => regime => addressWithoutID(NormalMode)(regime)
    case WhatIsYourPostcodePage                => regime => _ => Some(routes.SelectAddressController.onPageLoad(NormalMode, regime))
    case SelectAddressPage                     => regime => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode, regime))
    case BusinessTypePage                      => regime => _ => Some(routes.UTRController.onPageLoad(NormalMode, regime))
    case UTRPage                               => isSoleProprietor(NormalMode)
    case SoleNamePage                          => regime => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode, regime))
    case BusinessNamePage                      => regime => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode, regime))
    case IsThisYourBusinessPage                => isThisYourBusiness(NormalMode)
    case RegistrationInfoPage                  => registrationInfo(NormalMode)
    case _                                     => _ => _ => None
  }

  override val checkRouteMap: Page => Regime => UserAnswers => Option[Call] = {
    case DoYouHaveUniqueTaxPayerReferencePage => regime => doYouHaveUniqueTaxPayerReference(CheckMode)(regime)
    case BusinessTypePage                     => regime => _ => Some(routes.UTRController.onPageLoad(NormalMode, regime))
    case UTRPage                              => isSoleProprietor(CheckMode)
    case WhatIsYourNamePage                   => regime => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime))
    case WhatIsYourDateOfBirthPage            => whatIsYourDateOfBirthRoutes(CheckMode)
    case BusinessNamePage                     => regime => _ => Some(routes.IsThisYourBusinessController.onPageLoad(CheckMode, regime))
    case IsThisYourBusinessPage               => isThisYourBusiness(CheckMode)
    case _                                    => regime => _ => Some(Navigator.checkYourAnswers(regime))
  }

  private def registrationInfo(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(RegistrationInfoPage) match {
      case Some(info) if info.safeId != "" => Some(routes.IsThisYourBusinessController.onPageLoad(mode, regime))
      case _                               => Some(routes.NoRecordsMatchedController.onPageLoad(regime))
    }

  private def addressWithoutID(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) match {
      case Some(RegistrationTypeBusiness)   => Some(routes.ContactNameController.onPageLoad(mode, regime))
      case Some(RegistrationTypeIndividual) => Some(routes.ContactEmailController.onPageLoad(mode, regime))
      case None                             => Some(routes.SomeInformationIsMissingController.onPageLoad(regime))
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true  => routes.BusinessTypeController.onPageLoad(mode, regime)
      case false => jumpToCYA(mode, regime, routes.WhatAreYouRegisteringAsController.onPageLoad(mode, regime))
    }

  private def businessHaveDifferentNameRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessHaveDifferentNamePage) map {
      case true  => routes.WhatIsTradingNameController.onPageLoad(mode, regime)
      case false => routes.AddressWithoutIdController.onPageLoad(mode, regime)
    }

  private def whatAreYouRegisteringAs(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case RegistrationTypeBusiness   => routes.BusinessWithoutIDNameController.onPageLoad(mode, regime)
      case RegistrationTypeIndividual => routes.DoYouHaveNINController.onPageLoad(mode, regime)
    }

  private def doYouHaveNINORoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode, regime)
      case false => routes.NonUkNameController.onPageLoad(mode, regime)
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WeHaveConfirmedYourIdentityController.onPageLoad(regime)
      case false => routes.DoYouLiveInTheUKController.onPageLoad(mode, regime)
    }

  private def doYouLiveInTheUkRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouLiveInTheUKPage) map {
      case true  => routes.WhatIsYourPostcodeController.onPageLoad(mode, regime)
      case false => routes.AddressWithoutIdController.onPageLoad(mode, regime)
    }

  private def isSoleProprietor(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.Sole => routes.SoleNameController.onPageLoad(mode, regime)
      case _                 => routes.BusinessNameController.onPageLoad(mode, regime)
    }

  private def isThisYourBusiness(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(IsThisYourBusinessPage), ua.get(BusinessTypePage)) match {
      case (Some(true), _) if mode == CheckMode => Some(Navigator.checkYourAnswers(regime))
      case (Some(true), Some(Sole))             => Some(routes.ContactEmailController.onPageLoad(mode, regime))
      case (Some(true), Some(_))                => Some(routes.ContactNameController.onPageLoad(mode, regime))
      case _                                    => Some(routes.NoRecordsMatchedController.onPageLoad(regime))
    }
}
