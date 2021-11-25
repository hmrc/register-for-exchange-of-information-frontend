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
    case _                                     => _ => _ => None
  }

  override val checkRouteMap: Page => Regime => UserAnswers => Option[Call] = {
    // Individual with ID
    case DoYouHaveNINPage                      => regime => doYouHaveNINORoutes(CheckMode)(regime)
    case WhatIsYourNationalInsuranceNumberPage => regime => whatIsYourNationalInsuranceNumberRoutes(CheckMode)(regime)

    case DoYouHaveUniqueTaxPayerReferencePage => regime => doYouHaveUniqueTaxPayerReference(CheckMode)(regime)
    case WhatAreYouRegisteringAsPage          => regime => whatAreYouRegisteringAs(CheckMode)(regime)
    case BusinessWithoutIDNamePage            => regime => _ => Some(routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, regime))
    case BusinessHaveDifferentNamePage        => regime => businessHaveDifferentNameRoutes(CheckMode)(regime)
    case WhatIsTradingNamePage                => regime => whatIsTradingNameRoutes(CheckMode)(regime)
    case AddressWithoutIdPage                 => regime => addressWithoutID(CheckMode)(regime)
    case NonUkNamePage                        => regime => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime))
    case WhatIsYourNamePage                   => regime => whatIsYourNameRoutes(CheckMode)(regime)
    case WhatIsYourDateOfBirthPage            => whatIsYourDateOfBirthRoutes(CheckMode)
    case DoYouLiveInTheUKPage                 => regime => doYouLiveInTheUkRoutes(CheckMode)(regime)
    case WhatIsYourPostcodePage               => regime => _ => Some(routes.SelectAddressController.onPageLoad(CheckMode, regime))
    case SelectAddressPage                    => regime => addressWithoutID(CheckMode)(regime)
    case AddressUKPage                        => regime => addressWithoutID(CheckMode)(regime)
    case _                                    => regime => _ => Some(Navigator.checkYourAnswers(regime))
  }

  private def whatIsYourNationalInsuranceNumberRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    isContinueJourney(WhatIsYourNamePage, mode, ua) match {
      case true  => Some(routes.WhatIsYourNameController.onPageLoad(mode, regime))
      case false => Some(routes.CheckYourAnswersController.onPageLoad(regime))
    }

  private def whatIsYourNameRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    isContinueJourney(WhatIsYourDateOfBirthPage, mode, ua) match {
      case true  => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(mode, regime))
      case false => Some(routes.CheckYourAnswersController.onPageLoad(regime))
    }

  private def whatIsTradingNameRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), mode) match {
      case (_, CheckMode) if containsContactDetails(ua) => Some(routes.CheckYourAnswersController.onPageLoad(regime))
      case _                                            => Some(routes.AddressWithoutIdController.onPageLoad(mode, regime))
    }

  private def addressWithoutID(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(WhatAreYouRegisteringAsPage), mode) match {
      case (_, CheckMode) if containsContactDetails(ua) => Some(routes.CheckYourAnswersController.onPageLoad(regime))
      case (Some(RegistrationTypeBusiness), _)          => Some(routes.ContactNameController.onPageLoad(mode, regime))
      case (Some(RegistrationTypeIndividual), _)        => Some(routes.ContactEmailController.onPageLoad(mode, regime))
      case _                                            => Some(routes.SomeInformationIsMissingController.onPageLoad(regime))
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), mode) match {
      case (_, CheckMode) if containsContactDetails(ua) => Some(routes.CheckYourAnswersController.onPageLoad(regime))
      case (Some(true), _)                              => Some(routes.BusinessTypeController.onPageLoad(mode, regime))
      case (Some(false), _)                             => Some(routes.WhatAreYouRegisteringAsController.onPageLoad(mode, regime))
    }

  private def whatAreYouRegisteringAs(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(WhatAreYouRegisteringAsPage), mode) match {
      case (_, CheckMode) if containsContactDetails(ua) => Some(routes.CheckYourAnswersController.onPageLoad(regime))
      case (Some(RegistrationTypeBusiness), _)          => Some(routes.BusinessWithoutIDNameController.onPageLoad(mode, regime))
      case (Some(RegistrationTypeIndividual), _)        => Some(routes.DoYouHaveNINController.onPageLoad(mode, regime))
    }

  private def businessHaveDifferentNameRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    (ua.get(BusinessHaveDifferentNamePage), mode) match {
      case (Some(false), CheckMode) if containsContactDetails(ua) => Some(routes.CheckYourAnswersController.onPageLoad(regime))
      case (Some(true), _)                                        => Some(routes.WhatIsTradingNameController.onPageLoad(mode, regime))
      case (Some(false), _)                                       => Some(routes.AddressWithoutIdController.onPageLoad(mode, regime))
    }

  private def doYouHaveNINORoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true if isContinueJourney(WhatIsYourNationalInsuranceNumberPage, mode, ua) =>
        routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode, regime)
      case false if isContinueJourney(NonUkNamePage, mode, ua) => routes.NonUkNameController.onPageLoad(mode, regime)
      case _                                                   => routes.CheckYourAnswersController.onPageLoad(regime)
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WeHaveConfirmedYourIdentityController.onPageLoad(mode, regime)
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
      case (Some(true), Some(Sole)) => Some(routes.ContactEmailController.onPageLoad(mode, regime))
      case (Some(true), Some(_))    => Some(routes.ContactNameController.onPageLoad(mode, regime))
      case _                        => Some(routes.NoRecordsMatchedController.onPageLoad(regime))
    }

  // In CHECKMODE we check if contact details have been cleared down if not we can safely Redirec to CYA page
  private def containsContactDetails(ua: UserAnswers): Boolean = {
    val hasContactName = ua
      .get(ContactNamePage)
      .fold(false)(
        _ => true
      )
    val hasContactEmail = ua
      .get(ContactEmailPage)
      .fold(false)(
        _ => true
      )

    hasContactName || hasContactEmail
  }
}
