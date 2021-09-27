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
    case DoYouHaveUniqueTaxPayerReferencePage => isUTR(NormalMode)
    case BusinessTypePage                     => _ => Some(routes.UTRController.onPageLoad(NormalMode))
    case UTRPage                              => isSoleProprietor(NormalMode)
    case _                                    => _ => Some(routes.IndexController.onPageLoad())
  }

  override val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case _ => _ => Some(Navigator.checkYourAnswers)
  }

  private def isUTR(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUniqueTaxPayerReferencePage) map {
      case true  => routes.BusinessTypeController.onPageLoad(mode)
      case false => routes.IndexController.onPageLoad() // todo change once implemented
    }

  private def isSoleProprietor(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.Sole => routes.IndexController.onPageLoad() // todo your-name
      case _                 => routes.BusinessNameController.onPageLoad(mode)
    }
}
