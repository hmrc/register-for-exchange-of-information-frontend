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

package models.email

import models.BusinessType.Sole
import models.UserAnswers
import models.WhatAreYouRegisteringAs.RegistrationTypeBusiness
import pages.{BusinessTypePage, WhatAreYouRegisteringAsPage}

import javax.inject.Inject

class EmailUserType @Inject() ()() {

  def getUserTypeFromUa(userAnswers: UserAnswers): UserType =
    (userAnswers.get(WhatAreYouRegisteringAsPage), userAnswers.get(BusinessTypePage)) match {
      case (Some(registerType), None) => if (registerType == RegistrationTypeBusiness) Organisation else Individual
      case (None, Some(Sole))         => SoleTrader
      case (None, Some(_))            => Organisation
      case _                          => throw new RuntimeException("Cannot determine whether the registration should be Organisation or Individual")
    }
}
