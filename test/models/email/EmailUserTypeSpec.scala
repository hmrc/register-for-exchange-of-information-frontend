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
      "must return Organisation when UserAnswers has WhatAreYouRegisteringAs RegistrationTypeBusiness " in
      {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeBusiness)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Organisation
      }
      "must return Individual when UserAnswers has WhatAreYouRegisteringAs RegistrationTypeBusiness " in
      {
        val userAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUniqueTaxPayerReferencePage, false)
          .success
          .value
          .set(WhatAreYouRegisteringAsPage, RegistrationTypeIndividual)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Individual
      }
      "must return Organisation when UserAnswers has BusinessTypePage containing something other than Sole " in
      {
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
