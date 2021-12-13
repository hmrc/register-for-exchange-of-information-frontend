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

// @formatter:off
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
    case DoYouHaveUniqueTaxPayerReferencePage => regime => doYouHaveUniqueTaxPayerReference(CheckMode)(regime)
    case WhatAreYouRegisteringAsPage          => regime => whatAreYouRegisteringAs(CheckMode)(regime)
    case BusinessHaveDifferentNamePage        => regime => businessHaveDifferentNameRoutes(CheckMode)(regime)
    case AddressWithoutIdPage                 => regime => addressWithoutID(CheckMode)(regime)
    case DoYouHaveNINPage                     => regime => doYouHaveNINORoutes(CheckMode)(regime)
    case WhatIsYourDateOfBirthPage            => whatIsYourDateOfBirthRoutes(CheckMode)
    case DoYouLiveInTheUKPage                 => regime => doYouLiveInTheUkRoutes(CheckMode)(regime)
    case WhatIsYourPostcodePage               => regime => _ => Some(routes.SelectAddressController.onPageLoad(CheckMode, regime))
    case SelectAddressPage                    => regime => addressWithoutID(CheckMode)(regime)
    case AddressUKPage                        => regime => addressWithoutID(CheckMode)(regime)

    case BusinessWithoutIDNamePage => regime => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, regime, ua, BusinessHaveDifferentNamePage, routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, regime)
      )

    case WhatIsYourNationalInsuranceNumberPage => regime => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, regime, ua, WhatIsYourNamePage, routes.WhatIsYourNameController.onPageLoad(CheckMode, regime)
      )

    case WhatIsTradingNamePage => regime => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, regime, ua, AddressWithoutIdPage, routes.AddressWithoutIdController.onPageLoad(CheckMode, regime)
      )

    case NonUkNamePage => regime => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, regime, ua, WhatIsYourDateOfBirthPage, routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime)
      )

    case WhatIsYourNamePage => regime => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, regime, ua, WhatIsYourDateOfBirthPage, routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime)
      )

    case _ => regime => _ => Some(Navigator.checkYourAnswers(regime))
  }

  private def addressWithoutID(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case  RegistrationTypeBusiness =>
        checkNextPageForValueThenRoute(mode, regime, ua, ContactNamePage, routes.ContactNameController.onPageLoad(mode, regime)).get
      case  RegistrationTypeIndividual =>
        checkNextPageForValueThenRoute(mode, regime, ua, ContactEmailPage, routes.ContactEmailController.onPageLoad(mode, regime)).get
      case _ =>
        routes.SomeInformationIsMissingController.onPageLoad(regime)
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true =>
        checkNextPageForValueThenRoute(mode, regime, ua, BusinessTypePage, routes.BusinessTypeController.onPageLoad(mode, regime)).get
      case false =>
        checkNextPageForValueThenRoute(mode, regime, ua, WhatAreYouRegisteringAsPage, routes.WhatAreYouRegisteringAsController.onPageLoad(mode, regime)).get
    }

  private def whatAreYouRegisteringAs(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case RegistrationTypeBusiness =>
        checkNextPageForValueThenRoute(mode, regime, ua, BusinessWithoutIDNamePage, routes.BusinessWithoutIDNameController.onPageLoad(mode, regime)).get
      case RegistrationTypeIndividual =>
        checkNextPageForValueThenRoute(mode, regime, ua, DoYouHaveNINPage, routes.DoYouHaveNINController.onPageLoad(mode, regime)).get
    }

  private def businessHaveDifferentNameRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessHaveDifferentNamePage) map {
      case true  =>
        routes.WhatIsTradingNameController.onPageLoad(mode, regime)
      case false =>
        checkNextPageForValueThenRoute(mode, regime, ua, AddressWithoutIdPage, routes.AddressWithoutIdController.onPageLoad(mode, regime)).get
    }

  private def doYouHaveNINORoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true =>
        checkNextPageForValueThenRoute(mode, regime, ua, WhatIsYourNationalInsuranceNumberPage, routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode, regime)).get
      case false =>
        checkNextPageForValueThenRoute(mode, regime, ua, NonUkNamePage, routes.NonUkNameController.onPageLoad(mode, regime)).get
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(regime: Regime)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true => routes.WeHaveConfirmedYourIdentityController.onPageLoad(mode, regime)
      case false =>
        checkNextPageForValueThenRoute(mode, regime, ua, DoYouLiveInTheUKPage, routes.DoYouLiveInTheUKController.onPageLoad(mode, regime)).get
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
}

//
