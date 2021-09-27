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
class MDRNavigator @Inject() () extends Navigator {

  override val normalRoutes: Page => UserAnswers => Option[Call] = {
    case DoYouHaveUniqueTaxPayerReferencePage => doYouHaveUTRRoutes(NormalMode)
    case WhatAreYouRegisteringAsPage          => whatAreYouRegisteringAsRoutes(NormalMode)

    case _ => _ => Some(routes.IndexController.onPageLoad())
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case _ => _ => Some(Navigator.checkYourAnswers)
  }

  private def doYouHaveUTRRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true  => ??? // TODO - Change to What is your UTR when built
      case false => routes.WhatAreYouRegisteringAsController.onPageLoad(mode)
    }

  private def whatAreYouRegisteringAsRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(WhatAreYouRegisteringAsPage) map {
      case WhatAreYouRegisteringAs.RegistrationTypeBusiness   => ??? // TODO - Change to Business Journey when built
      case WhatAreYouRegisteringAs.RegistrationTypeIndividual => routes.DoYouHaveNINController.onPageLoad(mode)
    }

  private def doYouHaveNINORoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveNINPage) map {
      case true  => ??? // TODO - change to What is your NINO Journey when built
      case false => ??? // TODO - change to What is your individual name when built
    }

}
