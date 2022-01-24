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

package utils

import controllers.routes
import models.matching.OrgRegistrationInfo
import models.{CheckMode, Regime, UserAnswers}
import pages.{RegistrationInfoPage, SelectAddressPage, SndContactPhonePage}
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(val userAnswers: UserAnswers, val regime: Regime, val maxVisibleChars: Int = 100, countryListFactory: CountryListFactory)(implicit
  val messages: Messages
) extends RowBuilder {

  def confirmBusiness: Option[Row] = {
    val paragraphClass = """govuk-!-margin-0"""
    (userAnswers.get(pages.IsThisYourBusinessPage), userAnswers.get(RegistrationInfoPage)) match {
      case (Some(true), Some(registrationInfo: OrgRegistrationInfo)) =>
        val businessName = registrationInfo.name
        val address      = registrationInfo.address
        for {
          countryName <- countryListFactory.getDescriptionFromCode(address.countryCode)
        } yield toRow(
          msgKey = "businessWithIDName",
          value = Html(s"""
                  <p>$businessName</p>
                  <p class=$paragraphClass>${address.addressLine1}</p>
                  ${address.addressLine2.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                  ${address.addressLine3.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                  ${address.addressLine4.fold("")(
            address => s"<p class=$paragraphClass>$address</p>"
          )}
                 <p class=$paragraphClass>${address.postCodeFormatter(address.postalCode).getOrElse("")}</p>
                 ${if (address.countryCode.toUpperCase != "GB") s"<p $paragraphClass>$countryName</p>" else ""}
                  """),
          href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode, regime).url
        )
      case _ => None
    }
  }

  def whatIsTradingName: Option[Row] =
    userAnswers.get(pages.BusinessWithoutIDNamePage) map {
      x =>
        val value = userAnswers.get(pages.WhatIsTradingNamePage) match {
          case Some(answer) => answer
          case None         => "None"
        }

        toRow(
          msgKey = "whatIsTradingName",
          value = lit"$value",
          href = routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, regime).url
        )
    }

  def selectAddress: Option[Row] = userAnswers.get(SelectAddressPage) map {
    answer =>
      toRow(
        msgKey = "selectAddress",
        value = Html(s"${answer.replace(",", "<br>")}"),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, regime).url
      )
  }

  def businessWithoutIDName: Option[Row] = userAnswers.get(pages.BusinessWithoutIDNamePage) map {
    answer =>
      toRow(
        msgKey = "businessWithoutIDName",
        value = lit"$answer",
        href = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsYourPostcode: Option[Row] = userAnswers.get(pages.WhatIsYourPostcodePage) map {
    _ =>
      toRow(
        msgKey = "whatIsYourPostcode",
        value = msg"site.edit",
        href = routes.WhatIsYourPostcodeController.onPageLoad(CheckMode, regime).url
      )
  }

  def nonUkName: Option[Row] = userAnswers.get(pages.NonUkNamePage) map {
    answer =>
      toRow(
        msgKey = "nonUkName",
        value = lit"${answer.givenName} ${answer.familyName}",
        href = routes.NonUkNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def soleName: Option[Row] = userAnswers.get(pages.SoleNamePage) map {
    _ =>
      toRow(
        msgKey = "soleName",
        value = msg"site.edit",
        href = routes.SoleNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def addressUK: Option[Row] = userAnswers.get(pages.AddressUKPage) map {
    answer =>
      toRow(
        msgKey = "addressUK",
        value = formatAddress(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, regime).url
      )
  }

  def addressWithoutIdIndividual: Option[Row] = userAnswers.get(pages.AddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.individual",
        value = formatAddress(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, regime).url
      )
  }

  def addressWithoutIdBusiness: Option[Row] = userAnswers.get(pages.AddressWithoutIdPage) map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.business",
        value = formatAddress(answer),
        href = routes.AddressWithoutIdController.onPageLoad(CheckMode, regime).url
      )
  }

  def doYouLiveInTheUK(): Option[Row] = userAnswers.get(pages.DoYouLiveInTheUKPage) map {
    answer =>
      toRow(
        msgKey = "doYouLiveInTheUK",
        value = yesOrNo(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, regime).url
      )
  }

  def nonUkNameController: Option[Row] = userAnswers.get(pages.NonUkNamePage) map {
    _ =>
      toRow(
        msgKey = "nonUkNameController",
        value = msg"site.edit",
        href = routes.NonUkNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsYourDateOfBirth: Option[Row] = userAnswers.get(pages.WhatIsYourDateOfBirthPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourDateOfBirth",
        value = lit"${answer.format(dateFormatter)}",
        href = routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsYourName: Option[Row] = userAnswers.get(pages.WhatIsYourNamePage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourName",
        value = lit"${answer.firstName} ${answer.lastName}",
        href = routes.WhatIsYourNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def nino: Option[Row] = userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage) map {
    answer =>
      toRow(
        msgKey = "whatIsYourNationalInsuranceNumber",
        value = lit"$answer",
        href = routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode, regime).url
      )
  }

  def isThisYourBusiness: Option[Row] = userAnswers.get(pages.IsThisYourBusinessPage) map {
    _ =>
      toRow(
        msgKey = "isThisYourBusiness",
        value = msg"site.edit",
        href = routes.IsThisYourBusinessController.onPageLoad(CheckMode, regime).url
      )
  }

  def businessName: Option[Row] = userAnswers.get(pages.BusinessNamePage) map {
    answer =>
      toRow(
        msgKey = "businessName",
        value = msg"site.edit",
        href = routes.BusinessNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def uTR: Option[Row] = userAnswers.get(pages.UTRPage) map {
    _ =>
      toRow(
        msgKey = "uTR",
        value = msg"site.edit",
        href = routes.UTRController.onPageLoad(CheckMode, regime).url
      )
  }

  def businessType: Option[Row] = userAnswers.get(pages.BusinessTypePage) map {
    _ =>
      toRow(
        msgKey = "bussinessType",
        value = msg"site.edit",
        href = routes.BusinessTypeController.onPageLoad(CheckMode, regime).url
      )
  }

  def doYouHaveUniqueTaxPayerReference: Option[Row] = userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveUniqueTaxPayerReference",
        value = yesOrNo(answer),
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatAreYouRegisteringAs: Option[Row] = userAnswers.get(pages.WhatAreYouRegisteringAsPage) map {
    answer =>
      toRow(
        msgKey = "whatAreYouRegisteringAs",
        value = msg"whatAreYouRegisteringAs.$answer",
        href = routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode, regime).url
      )
  }

  def doYouHaveNIN: Option[Row] = userAnswers.get(pages.DoYouHaveNINPage) map {
    answer =>
      toRow(
        msgKey = "doYouHaveNIN",
        value = yesOrNo(answer),
        href = routes.DoYouHaveNINController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndConHavePhone: Option[Row] = userAnswers.get(pages.SndConHavePhonePage) map {
    answer =>
      toRow(
        msgKey = "sndConHavePhone",
        value = yesOrNo(answer),
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactPhone: Option[Row] = userAnswers.get(pages.SndConHavePhonePage) map {
    _ =>
      val value = userAnswers.get(SndContactPhonePage).getOrElse("None")
      toRow(
        msgKey = "sndContactPhone",
        value = lit"$value",
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactEmail: Option[Row] = userAnswers.get(pages.SndContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "sndContactEmail",
        value = lit"$answer",
        href = routes.SndContactEmailController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactName: Option[Row] = userAnswers.get(pages.SndContactNamePage) map {
    answer =>
      toRow(
        msgKey = "sndContactName",
        value = lit"$answer",
        href = routes.SndContactNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def secondContact: Option[Row] = userAnswers.get(pages.SecondContactPage) map {
    answer =>
      toRow(
        msgKey = "secondContact",
        value = yesOrNo(answer),
        href = routes.SecondContactController.onPageLoad(CheckMode, regime).url
      )
  }

  def contactPhone: Option[Row] = userAnswers.get(pages.ContactPhonePage) match {
    case Some(answer) => buildContactPhoneRow(answer)
    case None         => buildContactPhoneRow("None")
  }

  private def buildContactPhoneRow(value: String): Option[Row] =
    Some(
      toRow(
        msgKey = "contactPhone",
        value = lit"$value",
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode, regime).url
      )
    )

  def isContactTelephone: Option[Row] = userAnswers.get(pages.IsContactTelephonePage) map {
    answer =>
      toRow(
        msgKey = "isContactTelephone",
        value = yesOrNo(answer),
        href = routes.IsContactTelephoneController.onPageLoad(CheckMode, regime).url
      )
  }

  def contactName: Option[Row] = userAnswers.get(pages.ContactNamePage) map {
    answer =>
      toRow(
        msgKey = "contactName",
        value = lit"$answer",
        href = routes.ContactNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def contactEmail: Option[Row] = userAnswers.get(pages.ContactEmailPage) map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        value = lit"$answer",
        href = routes.ContactEmailController.onPageLoad(CheckMode, regime).url
      )
  }
}
