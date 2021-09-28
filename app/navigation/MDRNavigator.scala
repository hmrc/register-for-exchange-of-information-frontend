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
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class MDRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case DoYouHaveUniqueTaxPayerReferencePage  => doYouHaveUniqueTaxPayerReference(NormalMode)
    case WhatAreYouRegisteringAsPage           => whatAreYouRegisteringAs(NormalMode)
    case DoYouHaveNINPage                      => doYouHaveNINPage(NormalMode)
    case WhatIsYourNationalInsuranceNumberPage => _ => Some(routes.WhatIsYourNameController.onPageLoad(NormalMode))
    case WhatIsYourNamePage                    => _ => Some(routes.WhatIsYourDateOfBirthController.onPageLoad(NormalMode))
    case WhatIsYourDateOfBirthPage             => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case BusinessTypePage                      => _ => Some(routes.UTRController.onPageLoad(NormalMode))
    case UTRPage                               => isSoleProprietor(NormalMode)
    case BusinessNamePage                      => _ => Some(routes.IsThisYourBusinessController.onPageLoad(NormalMode))
    case IsThisYourBusinessPage                => isThisYourBusiness(NormalMode)
    case _                                     => _ => Some(routes.IndexController.onPageLoad())
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case _ => _ => Some(Navigator.checkYourAnswers)
  }

  private def doYouHaveUniqueTaxPayerReference(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true  => routes.BusinessTypeController.onPageLoad(mode)
      case false => routes.WhatAreYouRegisteringAsController.onPageLoad(mode)
    }

  private def whatAreYouRegisteringAs(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case RegistrationTypeBusiness   => routes.DoYouHaveNINController.onPageLoad(mode) // TODO replace with not yet implemented route
      case RegistrationTypeIndividual => routes.DoYouHaveNINController.onPageLoad(mode)
    }

  private def doYouHaveNINPage(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode) // TODO replace with not yet implemented route
      case false => routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(mode)
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.Sole => routes.WhatIsYourNameController.onPageLoad(mode)
      case _                 => routes.BusinessNameController.onPageLoad(mode)
    }

  private def isThisYourBusiness(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(IsThisYourBusinessPage) map {
      case true  => routes.NeedContactDetailsController.onPageLoad()
      case false => routes.WeCouldNotConfirmController.onPageLoad()
    }
}
