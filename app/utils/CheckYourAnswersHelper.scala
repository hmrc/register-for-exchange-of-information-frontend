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

package utils

import controllers.routes
import models.{Address, CheckMode, Name, NonUkName, Regime, UserAnswers, WhatAreYouRegisteringAs}
import pages.{RegistrationInfoPage, SndContactPhonePage}
import play.api.i18n.Messages
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._
import utils.CheckYourAnswersViewModel._

import java.time.LocalDate

class CheckYourAnswersHelper(val userAnswers: UserAnswers, val regime: Regime, val maxVisibleChars: Int = 100, countryListFactory: CountryListFactory)(implicit
  val messages: Messages
) extends RowBuilder {

  def buildBusinessWithID = BusinessWithID(userAnswers.get(pages.IsThisYourBusinessPage))(this)

  def buildBusinessWithoutID = BusinessWithoutID(
    userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage),
    userAnswers.get(pages.WhatAreYouRegisteringAsPage),
    userAnswers.get(pages.BusinessWithoutIDNamePage),
    userAnswers.get(pages.WhatIsTradingNamePage),
    userAnswers.get(pages.AddressWithoutIdPage)
  )(this)

  def buildIndividualWithID = IndividualWithID(
    userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage),
    userAnswers.get(pages.WhatAreYouRegisteringAsPage),
    userAnswers.get(pages.DoYouHaveNINPage),
    userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage),
    userAnswers.get(pages.WhatIsYourNamePage),
    userAnswers.get(pages.WhatIsYourDateOfBirthPage)
  )(this)

  def buildIndividualWithoutID = IndividualWithoutID(
    userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage),
    userAnswers.get(pages.WhatAreYouRegisteringAsPage),
    userAnswers.get(pages.DoYouHaveNINPage),
    userAnswers.get(pages.NonUkNamePage),
    userAnswers.get(pages.WhatIsYourDateOfBirthPage),
    userAnswers.get(pages.AddressWithoutIdPage),
    userAnswers.get(pages.AddressUKPage),
    userAnswers.get(pages.SelectAddressPage)
  )(this)

  def buildAllPages = AllPages(
    userAnswers.get(pages.DoYouHaveUniqueTaxPayerReferencePage),
    userAnswers.get(pages.IsThisYourBusinessPage),
    userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage),
    userAnswers.get(pages.WhatIsYourNamePage),
    userAnswers.get(pages.WhatIsYourDateOfBirthPage),
    userAnswers.get(pages.WhatAreYouRegisteringAsPage),
    userAnswers.get(pages.BusinessWithoutIDNamePage),
    userAnswers.get(pages.AddressUKPage),
    userAnswers.get(pages.DoYouHaveNINPage),
    userAnswers.get(pages.NonUkNamePage),
    userAnswers.get(pages.DoYouLiveInTheUKPage)
  )(this)

  def buildPagesToCheck = Tuple4(
    userAnswers.get(pages.BusinessTypePage),
    userAnswers.get(pages.WhatIsYourNationalInsuranceNumberPage),
    userAnswers.get(pages.BusinessWithoutIDNamePage),
    userAnswers.get(pages.NonUkNamePage)
  )

  def buildDetails: AsRowSeq =
    buildPagesToCheck match {
      case (Some(_), None, None, None) => buildBusinessWithID
      case (None, Some(_), None, None) => buildIndividualWithID
      case (None, None, Some(_), None) => buildBusinessWithoutID
      case (None, None, None, Some(_)) => buildIndividualWithoutID
      case _                           => buildAllPages
    }

  def buildFirstContact = FirstContact(
    userAnswers.get(pages.ContactNamePage),
    userAnswers.get(pages.ContactEmailPage),
    userAnswers.get(pages.ContactPhonePage)
  )(this)

  def buildSecondContact = SecondContact(
    userAnswers.get(pages.SecondContactPage),
    userAnswers.get(pages.SndContactNamePage),
    userAnswers.get(pages.SndContactEmailPage),
    userAnswers.get(pages.SndContactPhonePage)
  )(this)

  def confirmBusiness(confirmBusiness: Option[Boolean]): Option[Row] = {
    val paragraphClass = """govuk-!-margin-0"""
    confirmBusiness match {
      case Some(true) =>
        for {
          registrationInfo <- userAnswers.get(RegistrationInfoPage)
          businessName     <- registrationInfo.name
          address          <- registrationInfo.address
          countryName      <- countryListFactory.getDescriptionFromCode(address.countryCode)
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

  def doYouHaveUniqueTaxPayerReference(doYouHaveUniqueTaxPayerReference: Option[Boolean]) = doYouHaveUniqueTaxPayerReference map {
    answer =>
      toRow(
        msgKey = "doYouHaveUniqueTaxPayerReference",
        value = yesOrNo(answer),
        href = routes.DoYouHaveUniqueTaxPayerReferenceController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatAreYouRegisteringAs(whatAreYouRegisteringAs: Option[WhatAreYouRegisteringAs]): Option[Row] = whatAreYouRegisteringAs map {
    answer =>
      toRow(
        msgKey = "whatAreYouRegisteringAs",
        value = msg"whatAreYouRegisteringAs.$answer",
        href = routes.WhatAreYouRegisteringAsController.onPageLoad(CheckMode, regime).url
      )
  }

  def businessWithoutIDName(businessWithoutIDName: Option[String]) = businessWithoutIDName map {
    answer =>
      toRow(
        msgKey = "businessWithoutIDName",
        value = lit"$answer",
        href = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsTradingName(whatIsTradingName: Option[String]): Option[Row] = {

    val value = whatIsTradingName match {
      case Some(answer) => answer
      case None         => "None"
    }

    Some(
      toRow(
        msgKey = "whatIsTradingName",
        value = lit"$value",
        href = routes.BusinessHaveDifferentNameController.onPageLoad(CheckMode, regime).url
      )
    )
  }

  def addressWithoutIdBusiness(addressWithoutIdBusiness: Option[Address]): Option[Row] = addressWithoutIdBusiness map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.business",
        value = formatAddress(answer),
        href = routes.AddressWithoutIdController.onPageLoad(CheckMode, regime).url
      )
  }

  def doYouHaveNIN(doYouHaveNIN: Option[Boolean]) = doYouHaveNIN map {
    answer =>
      toRow(
        msgKey = "doYouHaveNIN",
        value = yesOrNo(answer),
        href = routes.DoYouHaveNINController.onPageLoad(CheckMode, regime).url
      )
  }

  def nino(nino: Option[Nino]): Option[Row] = nino map {
    answer =>
      toRow(
        msgKey = "whatIsYourNationalInsuranceNumber",
        value = lit"$answer",
        href = routes.WhatIsYourNationalInsuranceNumberController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsYourName(whatIsYourName: Option[Name]) = whatIsYourName map {
    answer =>
      toRow(
        msgKey = "whatIsYourName",
        value = lit"${answer.firstName} ${answer.lastName}",
        href = routes.WhatIsYourNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def whatIsYourDateOfBirth(whatIsYourDateOfBirth: Option[LocalDate]) = whatIsYourDateOfBirth map {
    answer =>
      toRow(
        msgKey = "whatIsYourDateOfBirth",
        value = lit"${answer.format(dateFormatter)}",
        href = routes.WhatIsYourDateOfBirthController.onPageLoad(CheckMode, regime).url
      )
  }

  def nonUkName(nonUkName: Option[NonUkName]) = nonUkName map {
    answer =>
      toRow(
        msgKey = "nonUkName",
        value = lit"${answer.givenName} ${answer.familyName}",
        href = routes.NonUkNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def addressWithoutIdIndividual(addressWithoutIdIndividual: Option[Address]) = addressWithoutIdIndividual map {
    answer =>
      toRow(
        msgKey = "addressWithoutId.individual",
        value = formatAddress(answer),
        href = routes.AddressWithoutIdController.onPageLoad(CheckMode, regime).url
      )
  }

  def addressUK(addressUK: Option[Address]) = addressUK map {
    answer =>
      toRow(
        msgKey = "addressUK",
        value = formatAddress(answer),
        href = routes.AddressUKController.onPageLoad(CheckMode, regime).url
      )
  }

  def selectAddress(selectAddress: Option[String]) = selectAddress map {
    answer =>
      toRow(
        msgKey = "selectAddress",
        value = Html(s"${answer.replace(",", "<br>")}"),
        href = routes.SelectAddressController.onPageLoad(CheckMode, regime).url
      )
  }

  def doYouLiveInTheUK(doYouLiveInTheUK: Option[Boolean]) = doYouLiveInTheUK map {
    answer =>
      toRow(
        msgKey = "doYouLiveInTheUK",
        value = yesOrNo(answer),
        href = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode, regime).url
      )
  }

  /*
    First contact
   */
  def contactName(contactName: Option[String]): Option[Row] = contactName map {
    answer =>
      toRow(
        msgKey = "contactName",
        value = lit"$answer",
        href = routes.ContactNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def contactEmail(contactEmail: Option[String]) = contactEmail map {
    answer =>
      toRow(
        msgKey = "contactEmail",
        value = lit"$answer",
        href = routes.ContactEmailController.onPageLoad(CheckMode, regime).url
      )
  }

  def contactPhone(contactPhone: Option[String]) = contactPhone match {
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

  /*
    Second Contact
   */
  def secondContact(secondContact: Option[Boolean]) = secondContact map {
    answer =>
      toRow(
        msgKey = "secondContact",
        value = yesOrNo(answer),
        href = routes.SecondContactController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactName(sndContactName: Option[String]) = sndContactName map {
    answer =>
      toRow(
        msgKey = "sndContactName",
        value = lit"$answer",
        href = routes.SndContactNameController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactEmail(sndContactEmail: Option[String]) = sndContactEmail map {
    answer =>
      toRow(
        msgKey = "sndContactEmail",
        value = lit"$answer",
        href = routes.SndContactEmailController.onPageLoad(CheckMode, regime).url
      )
  }

  def sndContactPhone(sndContactPhone: Option[String]) = sndContactPhone map {
    _ =>
      val value = userAnswers.get(SndContactPhonePage).getOrElse("None")
      toRow(
        msgKey = "sndContactPhone",
        value = lit"$value",
        href = routes.SndConHavePhoneController.onPageLoad(CheckMode, regime).url
      )
  }
}
