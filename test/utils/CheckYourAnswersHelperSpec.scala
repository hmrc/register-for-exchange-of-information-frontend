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

package utils

import base.SpecBase
import models.matching.{OrgRegistrationInfo, SafeId}
import models.register.response.details.AddressResponse
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.{AutoMatchedUTRPage, IsThisYourBusinessPage, RegistrationInfoPage}
import play.api.test.Helpers
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Key, SummaryListRow, Value}

class CheckYourAnswersHelperSpec extends SpecBase with GuiceOneAppPerSuite {

  val addressResponse: AddressResponse = AddressResponse("line1", Some("line2"), None, None, Some("NE98 1ZZ"), "GB")

  val maxChars = 100

  val mockCountryListFactory: CountryListFactory = mock[CountryListFactory]

  "CheckYourAnswersHelper" - {

    "confirmBusiness must return a SummaryListRow with the business details with href to change-reporter-type when AutoMatchedUTR is not set" in {
      val userAnswers = emptyUserAnswers
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, OrgRegistrationInfo(SafeId("SafeId"), "name", addressResponse))
        .success
        .value

      when(mockCountryListFactory.getDescriptionFromCode(any())).thenReturn(Some("United Kingdom"))

      val service = new CheckYourAnswersHelper(userAnswers, maxChars, mockCountryListFactory)(Helpers.stubMessages())

      service.confirmBusiness mustBe Some(
        createSummaryListRow("/register-for-mdr/register/change-reporter-type")
      )
    }

    "confirmBusiness must return a SummaryListRow with the business details with href to change-is-this-your-business when AutoMatchedUTR is set" in {
      val userAnswers = emptyUserAnswers
        .set(AutoMatchedUTRPage, utr)
        .success
        .value
        .set(IsThisYourBusinessPage, true)
        .success
        .value
        .set(RegistrationInfoPage, OrgRegistrationInfo(SafeId("SafeId"), "name", addressResponse))
        .success
        .value

      when(mockCountryListFactory.getDescriptionFromCode(any())).thenReturn(Some("United Kingdom"))

      val service = new CheckYourAnswersHelper(userAnswers, maxChars, mockCountryListFactory)(Helpers.stubMessages())

      service.confirmBusiness mustBe Some(createSummaryListRow("/register-for-mdr/register/change-is-this-your-business"))
    }

    "confirmBusiness must return Non when the business details don't exist" in {
      val service = new CheckYourAnswersHelper(emptyUserAnswers, maxChars, mockCountryListFactory)(Helpers.stubMessages())

      service.confirmBusiness mustBe None
    }
  }

  private def createSummaryListRow(href: String) =
    SummaryListRow(
      Key(Text("businessWithIDName.checkYourAnswersLabel"), "govuk-!-width-one-half"),
      Value(
        HtmlContent(
          """
            |<p>name</p>
            |<p class=govuk-!-margin-0>line1</p>
            |<p class=govuk-!-margin-0>line2</p>
            |
            |
            |<p class=govuk-!-margin-0>NE98  1ZZ</p>
            |
            |""".stripMargin
        ),
        ""
      ),
      "",
      Some(
        Actions(
          "",
          List(
            ActionItem(
              href,
              HtmlContent(
                """
                  |<span aria-hidden="true">site.edit</span>
                  |<span class="govuk-visually-hidden">businessWithIDName.checkYourAnswersLabel</span>
                  |""".stripMargin
              ),
              None,
              "",
              Map("id" -> "business-with-i-d-name")
            )
          )
        )
      )
    )
}
