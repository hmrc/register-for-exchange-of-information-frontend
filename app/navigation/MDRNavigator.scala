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
import models.ReporterType.{Individual, Sole}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

// @formatter:off
@Singleton
class MDRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case ReporterTypePage                       => whatAreYouReportingAs(NormalMode)
    case RegisteredAddressInUKPage              => isRegisteredAddressInUk(NormalMode)
    case UTRPage                                => isSoleProprietor(NormalMode)
    case BusinessNamePage                       => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case SoleNamePage                           => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case IsThisYourBusinessPage                 => isThisYourBusiness(NormalMode)

    case DoYouHaveUniqueTaxPayerReferencePage   => doYouHaveUniqueTaxPayerReference(NormalMode)
    case BusinessWithoutIDNamePage              => _ => Some(routes.BusinessHaveDifferentNameController.onPageLoad(NormalMode))
    case BusinessHaveDifferentNamePage          => businessHaveDifferentNameRoutes(NormalMode)
    case WhatIsTradingNamePage                  => _ => Some(routes.BusinessAddressWithoutIdController.onPageLoad(NormalMode))
    case BusinessAddressWithoutIdPage           => _ => Some(routes.YourContactDetailsController.onPageLoad(NormalMode))

    case DoYouHaveNINPage                       => doYouHaveNINORoutes(NormalMode)
    case WhatIsYourNationalInsuranceNumberPage  => _ => Some(routes.WhatIsYourNameController.onPageLoad(NormalMode))
    case WhatIsYourNamePage                     => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
    case WhatIsYourDateOfBirthPage              => whatIsYourDateOfBirthRoutes(NormalMode)

    case NonUkNamePage                          => _ => Some(routes.DateOfBirthWithoutIdController.onPageLoad(NormalMode))
    case DateOfBirthWithoutIdPage               => whatIsYourDateOfBirthRoutes(NormalMode)
    case DoYouLiveInTheUKPage                   => doYouLiveInTheUkRoutes(NormalMode)
    case WhatIsYourPostcodePage                 => _ => Some(routes.SelectAddressController.onPageLoad(NormalMode))
    case SelectAddressPage                      => _ => Some(routes.IndividualContactEmailController.onPageLoad(NormalMode))
    case AddressUKPage                          => _ => Some(routes.IndividualContactEmailController.onPageLoad(NormalMode))
    case IndividualAddressWithoutIdPage         => _ => Some(routes.IndividualContactEmailController.onPageLoad(NormalMode))
    case RegistrationInfoPage                   => _ => Some(routes.IndividualContactEmailController.onPageLoad(NormalMode))
    case _                                      => _ => None
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case ReporterTypePage                       => whatAreYouReportingAs(CheckMode)
    case RegisteredAddressInUKPage              => isRegisteredAddressInUk(CheckMode)
    case UTRPage                                => isSoleProprietor(CheckMode)
    case IsThisYourBusinessPage                 => isThisYourBusiness(CheckMode)
    case DoYouHaveUniqueTaxPayerReferencePage   => doYouHaveUniqueTaxPayerReference(CheckMode)
    case BusinessHaveDifferentNamePage          => businessHaveDifferentNameRoutes(CheckMode)
    case DoYouHaveNINPage                       => doYouHaveNINORoutes(CheckMode)
    case WhatIsYourNationalInsuranceNumberPage  => _ => Some(routes.WhatIsYourNameController.onPageLoad(CheckMode))
    case WhatIsYourNamePage                     => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode))
    case WhatIsYourDateOfBirthPage              => whatIsYourDateOfBirthRoutes(CheckMode)
    case DateOfBirthWithoutIdPage               => whatIsYourDateOfBirthRoutes(CheckMode)
    case DoYouLiveInTheUKPage                   => doYouLiveInTheUkRoutes(CheckMode)
    case WhatIsYourPostcodePage                 => _ => Some(routes.SelectAddressController.onPageLoad(CheckMode))

    case BusinessNamePage  => ua =>
      checkNextPageForValueThenRoute(CheckMode, ua, RegistrationInfoPage, routes.IsThisYourBusinessController.onPageLoad(CheckMode)
      )

    case SoleNamePage  => ua =>
      checkNextPageForValueThenRoute(CheckMode, ua, RegistrationInfoPage, routes.IsThisYourBusinessController.onPageLoad(CheckMode)
      )

    case BusinessWithoutIDNamePage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, BusinessHaveDifferentNamePage, routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode)
      )

    case WhatIsTradingNamePage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, BusinessAddressWithoutIdPage, routes.BusinessAddressWithoutIdController.onPageLoad(CheckMode)
      )

    case RegistrationInfoPage                  => ua =>
      checkNextPageForValueThenRoute(CheckMode, ua, IndividualContactEmailPage, routes.IndividualContactEmailController.onPageLoad(CheckMode)
      )

    case ContactNamePage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, ContactEmailPage, routes.ContactEmailController.onPageLoad(CheckMode)
      )

    case NonUkNamePage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, DateOfBirthWithoutIdPage, routes.DateOfBirthWithoutIdController.onPageLoad(CheckMode)
      )

    case IndividualAddressWithoutIdPage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, IndividualContactEmailPage, routes.IndividualContactEmailController.onPageLoad(CheckMode)
      )

    case SelectAddressPage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, IndividualContactEmailPage, routes.IndividualContactEmailController.onPageLoad(CheckMode)
      )

    case AddressUKPage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, IndividualContactEmailPage, routes.IndividualContactEmailController.onPageLoad(CheckMode)
    )

    case BusinessAddressWithoutIdPage  => ua =>
      checkNextPageForValueThenRoute(
        CheckMode, ua, ContactNamePage, routes.YourContactDetailsController.onPageLoad(CheckMode)
      )

    case _  => _ => Some(Navigator.checkYourAnswers)
  }

  private def whatAreYouReportingAs(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(ReporterTypePage) map {
      case Individual => routes.DoYouHaveNINController.onPageLoad(mode)
      case _ => routes.RegisteredAddressInUKController.onPageLoad(mode)
    }

  private def isRegisteredAddressInUk(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(RegisteredAddressInUKPage) map {
      case true => routes.UTRController.onPageLoad(mode)
      case false => routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(mode)
    }
  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(DoYouHaveUniqueTaxPayerReferencePage), ua.get(ReporterTypePage)) match {
      case (Some(true), _) => Some(routes.UTRController.onPageLoad(mode))
      case (Some(false), Some(Sole)) => Some(routes.DoYouHaveNINController.onPageLoad(mode))
      case (Some(false), Some(_)) => Some(routes.BusinessWithoutIDNameController.onPageLoad(mode))
    }

  private def businessHaveDifferentNameRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessHaveDifferentNamePage) map {
      case true  =>
        routes.WhatIsTradingNameController.onPageLoad(mode)
      case false =>
        checkNextPageForValueThenRoute(mode, ua, BusinessAddressWithoutIdPage, routes.BusinessAddressWithoutIdController.onPageLoad(mode)).get
    }

  private def doYouHaveNINORoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true =>
        checkNextPageForValueThenRoute(mode, ua, WhatIsYourNationalInsuranceNumberPage,
          routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode)).get
      case false =>
        checkNextPageForValueThenRoute(mode, ua, NonUkNamePage, routes.NonUkNameController.onPageLoad(mode)).get
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true =>
        routes.WeHaveConfirmedYourIdentityController.onPageLoad(mode)
      case false =>
        checkNextPageForValueThenRoute(mode, ua, DoYouLiveInTheUKPage, routes.DoYouLiveInTheUKController.onPageLoad(mode)).get
    }

  private def doYouLiveInTheUkRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouLiveInTheUKPage) map {
      case true  => routes.WhatIsYourPostcodeController.onPageLoad(mode)
      case false => routes.IndividualAddressWithoutIdController.onPageLoad(mode)
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(ReporterTypePage) map {
      case Sole => routes.SoleNameController.onPageLoad(mode)
      case _    => routes.BusinessNameController.onPageLoad(mode)
    }

  private def isThisYourBusiness(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(IsThisYourBusinessPage), ua.get(ReporterTypePage)) match {
      case (Some(true), Some(Sole)) =>
        checkNextPageForValueThenRoute(mode, ua, IndividualContactEmailPage, routes.IndividualContactEmailController.onPageLoad(mode))
      case (Some(true), _)    =>
        checkNextPageForValueThenRoute(mode, ua, ContactNamePage, routes.YourContactDetailsController.onPageLoad(mode))
      case _                        => Some(routes.BusinessNotIdentifiedController.onPageLoad())
    }
}

//
