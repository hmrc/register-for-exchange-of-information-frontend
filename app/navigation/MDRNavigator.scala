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
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import javax.inject.{Inject, Singleton}

@Singleton
class MDRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case DoYouHaveUniqueTaxPayerReferencePage  => doYouHaveUniqueTaxPayerReference(NormalMode)
    case WhatAreYouRegisteringAsPage           => whatAreYouRegisteringAs(NormalMode)
    case DoYouHaveNINPage                      => doYouHaveNINORoutes(NormalMode)
    case WhatIsYourNationalInsuranceNumberPage => _ => Some(routes.WhatIsYourNameController.onPageLoad(NormalMode))
    case WhatIsYourNamePage                    => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
    case WhatIsYourDateOfBirthPage             => whatIsYourDateOfBirthRoutes(NormalMode)
    case NonUkNamePage                         => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
    case DoYouLiveInTheUKPage                  => doYouLiveInTheUkRoutes(NormalMode)
    case AddressUKPage                         => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode))
    case AddressWithoutIdPage                  => addressWithoutID(NormalMode)
    case WhatIsYourPostcodePage                => _ => Some(routes.SelectAddressController.onPageLoad(NormalMode))
    case SelectAddressPage                     => _ => Some(routes.ContactEmailController.onPageLoad(NormalMode))
    case BusinessWithoutIDNamePage             => _ => Some(routes.AddressWithoutIdController.onPageLoad(NormalMode))
    case BusinessTypePage                      => _ => Some(routes.UTRController.onPageLoad(NormalMode))
    case UTRPage                               => isSoleProprietor(NormalMode)
    case SoleNamePage                          => _ => Some(routes.SoleDateOfBirthController.onPageLoad(NormalMode))
    case SoleDateOfBirthPage                   => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case BusinessNamePage                      => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case IsThisYourBusinessPage                => isThisYourBusiness(NormalMode)
    case _                                     => _ => Some(routes.IndexController.onPageLoad())
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case _ => _ => Some(Navigator.checkYourAnswers)
  }

  private def addressWithoutID(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) match {
      case Some(RegistrationTypeBusiness)   => Some(routes.ContactNameController.onPageLoad(mode))
      case Some(RegistrationTypeIndividual) => Some(routes.ContactEmailController.onPageLoad(mode))
      case None                             => Some(routes.SomeInformationIsMissingController.onPageLoad())
    }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true  => routes.BusinessTypeController.onPageLoad(mode)
      case false => routes.WhatAreYouRegisteringAsController.onPageLoad(mode)
    }

  private def whatAreYouRegisteringAs(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case RegistrationTypeBusiness   => routes.BusinessWithoutIDNameController.onPageLoad(mode)
      case RegistrationTypeIndividual => routes.DoYouHaveNINController.onPageLoad(mode)
    }

  private def doYouHaveNINORoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode)
      case false => routes.NonUkNameController.onPageLoad(mode)
    }

  private def whatIsYourDateOfBirthRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WeHaveConfirmedYourIdentityController.onPageLoad()
      case false => routes.DoYouLiveInTheUKController.onPageLoad(mode)
    }

  private def doYouLiveInTheUkRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouLiveInTheUKPage) map {
      case true  => routes.WhatIsYourPostcodeController.onPageLoad(mode)
      case false => routes.AddressWithoutIdController.onPageLoad(mode)
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.Sole => routes.SoleNameController.onPageLoad(mode)
      case _                 => routes.BusinessNameController.onPageLoad(mode)
    }

  private def isThisYourBusiness(mode: Mode)(ua: UserAnswers): Option[Call] =
    Option(ua.get(IsThisYourBusinessPage), ua.get(BusinessTypePage)) map {
      case (Some(true), Some(Sole))  => routes.ContactEmailController.onPageLoad(mode)
      case (Some(true), Some(_))     => routes.ContactNameController.onPageLoad(mode)
      case (Some(true), None)        => routes.WeCouldNotConfirmController.onPageLoad("identity")
      case (Some(false), Some(Sole)) => routes.WeCouldNotConfirmController.onPageLoad("identity")
      case (Some(false), _)          => routes.WeCouldNotConfirmController.onPageLoad("organisation")
    }
}
