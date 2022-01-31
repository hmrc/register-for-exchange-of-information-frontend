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

import base.SpecBase
import models.BusinessType.{Partnership, Sole}
import models.UserAnswers
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import pages.{BusinessTypePage, DoYouHaveUniqueTaxPayerReferencePage, WhatAreYouRegisteringAsPage}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class EmailUserTypeSpec extends SpecBase {

  "EmailUserTypeSpec" - {
    lazy val app: Application = new GuiceApplicationBuilder()
      .build()

    val emailUserType = app.injector.instanceOf[EmailUserType]

    "getUserTypeFromUa" - {
      "must return Sole when UserAnswers has BusinessTypePage containing Sole" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(BusinessTypePage, Sole)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe SoleTrader
      }
      "must return Organisation when UserAnswers has WhatAreYouRegisteringAs RegistrationTypeBusiness " in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Organisation
      }
      "must return Individual when UserAnswers has WhatAreYouRegisteringAs RegistrationTypeBusiness " in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Individual
      }
      "must return Organisation when UserAnswers has BusinessTypePage containing something other than Sole " in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(BusinessTypePage, Partnership)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Organisation
      }
    }
    "Throws exception when userAnswers does not contain BusinessTypePage or WhatAreYouRegisteringAs" in {
      val userAnswers = UserAnswers(userAnswersId)
      assertThrows[RuntimeException] {
        emailUserType.getUserTypeFromUa(userAnswers)
      }
    }
  }
}
