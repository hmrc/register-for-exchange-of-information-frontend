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

package models.email

import base.SpecBase
import models.ReporterType
import models.ReporterType.Sole
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import pages.{AutoMatchedUTRPage, DoYouHaveUniqueTaxPayerReferencePage, ReporterTypePage}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class EmailUserTypeSpec extends SpecBase {

  "EmailUserTypeSpec" - {
    lazy val app: Application = new GuiceApplicationBuilder()
      .build()

    val emailUserType = app.injector.instanceOf[EmailUserType]

    "getUserTypeFromUa" - {
      "must return Sole when UserAnswers has ReporterTypePage containing Sole" in {
        val userAnswers = emptyUserAnswers
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ReporterTypePage, Sole)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe SoleTrader
      }

      "must return Individual when UserAnswers has ReporterType containing Individual " in {
        val userAnswers = emptyUserAnswers
          .set(ReporterTypePage, ReporterType.Individual)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Individual
      }
      "must return Organisation when UserAnswers has ReporterTypePage containing something other than Sole or Individual" in {
        val reporterType = Gen
          .oneOf(ReporterType.values)
          .filterNot(reporter => reporter == Sole || reporter == ReporterType.Individual)
          .pureApply(Gen.Parameters.default, Seed.random())
        val userAnswers  = emptyUserAnswers
          .set(DoYouHaveUniqueTaxPayerReferencePage, true)
          .success
          .value
          .set(ReporterTypePage, reporterType)
          .success
          .value

        emailUserType.getUserTypeFromUa(userAnswers) mustBe Organisation
      }
    }

    "must default to Organisation when userAnswers does not contain ReporterTypePage but has AutoMatchedUtr set" in {
      val userAnswers = emptyUserAnswers.set(AutoMatchedUTRPage, utr).success.value

      emailUserType.getUserTypeFromUa(userAnswers) mustBe Organisation
    }

    "Throws exception when userAnswers does not contain ReporterTypePage and AutoMatchedUtr" in {
      val userAnswers = emptyUserAnswers
      assertThrows[RuntimeException] {
        emailUserType.getUserTypeFromUa(userAnswers)
      }
    }
  }
}
