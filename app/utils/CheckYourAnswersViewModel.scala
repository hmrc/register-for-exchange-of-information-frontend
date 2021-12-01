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

import models.matching.RegistrationInfo
import models.{Address, Name, NonUkName, WhatAreYouRegisteringAs}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.viewmodels._

import java.time.LocalDate

sealed trait AsRowSeq {
  def asRowSeq: Seq[SummaryList.Row]
}

object CheckYourAnswersViewModel {

  case class BusinessWithID(confirmBusiness: Option[Boolean], registrationInfo: Option[RegistrationInfo])(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(check.confirmBusinessRow(confirmBusiness, registrationInfo)).flatten
  }

  case class BusinessWithoutID(doYouHaveUniqueTaxPayerReference: Option[Boolean],
                               whatAreYouRegisteringAs: Option[WhatAreYouRegisteringAs],
                               businessWithoutIDName: Option[String],
                               whatIsTradingName: Option[String],
                               addressWithoutIdBusiness: Option[Address]
  )(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(
        check.doYouHaveUniqueTaxPayerReferenceRow(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAsRow(whatAreYouRegisteringAs),
        check.businessWithoutIDNameRow(businessWithoutIDName),
        check.whatIsTradingNameRow(whatIsTradingName),
        check.addressWithoutIdBusinessRow(addressWithoutIdBusiness)
      ).flatten
  }

  case class IndividualWithID(doYouHaveUniqueTaxPayerReference: Option[Boolean],
                              whatAreYouRegisteringAs: Option[WhatAreYouRegisteringAs],
                              doYouHaveNIN: Option[Boolean],
                              nino: Option[Nino],
                              whatIsYourName: Option[Name],
                              whatIsYourDateOfBirth: Option[LocalDate]
  )(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(
        check.doYouHaveUniqueTaxPayerReferenceRow(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAsRow(whatAreYouRegisteringAs),
        check.doYouHaveNINRow(doYouHaveNIN),
        check.ninoRow(nino),
        check.whatIsYourNameRow(whatIsYourName),
        check.whatIsYourDateOfBirthRow(whatIsYourDateOfBirth)
      ).flatten
  }

  case class IndividualWithoutID(doYouHaveUniqueTaxPayerReference: Option[Boolean],
                                 whatAreYouRegisteringAs: Option[WhatAreYouRegisteringAs],
                                 doYouHaveNIN: Option[Boolean],
                                 nonUkName: Option[NonUkName],
                                 whatIsYourDateOfBirth: Option[LocalDate],
                                 addressWithoutIdIndividual: Option[Address],
                                 addressUK: Option[Address],
                                 selectAddress: Option[String]
  )(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(
        check.doYouHaveUniqueTaxPayerReferenceRow(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAsRow(whatAreYouRegisteringAs),
        check.doYouHaveNINRow(doYouHaveNIN),
        check.nonUkNameRow(nonUkName),
        check.whatIsYourDateOfBirthRow(whatIsYourDateOfBirth),
        check.addressWithoutIdIndividualRow(addressWithoutIdIndividual),
        check.addressUKRow(addressUK),
        check.selectAddressRow(selectAddress)
      ).flatten
  }

  case class AllPages(doYouHaveUniqueTaxPayerReference: Option[Boolean],
                      confirmBusiness: Option[Boolean],
                      registrationInfo: Option[RegistrationInfo],
                      nino: Option[Nino],
                      whatIsYourName: Option[Name],
                      whatIsYourDateOfBirth: Option[LocalDate],
                      whatAreYouRegisteringAs: Option[WhatAreYouRegisteringAs],
                      businessWithoutIDName: Option[String],
                      addressUK: Option[Address],
                      doYouHaveNIN: Option[Boolean],
                      nonUkName: Option[NonUkName],
                      doYouLiveInTheUK: Option[Boolean]
  )(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(
        check.doYouHaveUniqueTaxPayerReferenceRow(doYouHaveUniqueTaxPayerReference),
        check.confirmBusinessRow(confirmBusiness, registrationInfo),
        check.ninoRow(nino),
        check.whatIsYourNameRow(whatIsYourName),
        check.whatIsYourDateOfBirthRow(whatIsYourDateOfBirth),
        check.whatAreYouRegisteringAsRow(whatAreYouRegisteringAs),
        check.businessWithoutIDNameRow(businessWithoutIDName),
        check.addressUKRow(addressUK),
        check.doYouHaveNINRow(doYouHaveNIN),
        check.nonUkNameRow(nonUkName),
        check.doYouLiveInTheUK(doYouLiveInTheUK)
      ).flatten
  }

  case class FirstContact(contactName: Option[String], contactEmail: Option[String], contactPhone: Option[String])(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(check.contactNameRow(contactName), check.contactEmailRow(contactEmail), check.contactPhoneRow(contactPhone)).flatten
  }

  case class SecondContact(secondContact: Option[Boolean], sndContactName: Option[String], sndContactEmail: Option[String], sndContactPhone: Option[String])(
    implicit check: CheckYourAnswersHelper
  ) extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(
        check.secondContactRow(secondContact),
        check.sndContactNameRow(sndContactName),
        check.sndContactEmailRow(sndContactEmail),
        check.sndContactPhoneRow(sndContactPhone)
      ).flatten
  }
}
