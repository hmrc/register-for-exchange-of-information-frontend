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

import models.{Address, Name, NonUkName, WhatAreYouRegisteringAs}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.viewmodels._

import java.time.LocalDate

sealed trait AsRowSeq {
  def asRowSeq: Seq[SummaryList.Row]
}

object CheckYourAnswersViewModel {

  case class BusinessWithID(confirmBusiness: Option[Boolean])(implicit check: CheckYourAnswersHelper) extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(check.confirmBusiness(confirmBusiness)).flatten
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
        check.doYouHaveUniqueTaxPayerReference(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAs(whatAreYouRegisteringAs),
        check.businessWithoutIDName(businessWithoutIDName),
        check.whatIsTradingName(whatIsTradingName),
        check.addressWithoutIdBusiness(addressWithoutIdBusiness)
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
        check.doYouHaveUniqueTaxPayerReference(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAs(whatAreYouRegisteringAs),
        check.doYouHaveNIN(doYouHaveNIN),
        check.nino(nino),
        check.whatIsYourName(whatIsYourName),
        check.whatIsYourDateOfBirth(whatIsYourDateOfBirth)
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
        check.doYouHaveUniqueTaxPayerReference(doYouHaveUniqueTaxPayerReference),
        check.whatAreYouRegisteringAs(whatAreYouRegisteringAs),
        check.doYouHaveNIN(doYouHaveNIN),
        check.nonUkName(nonUkName),
        check.whatIsYourDateOfBirth(whatIsYourDateOfBirth),
        check.addressWithoutIdIndividual(addressWithoutIdIndividual),
        check.addressUK(addressUK),
        check.selectAddress(selectAddress)
      ).flatten
  }

  case class AllPages(doYouHaveUniqueTaxPayerReference: Option[Boolean],
                      confirmBusiness: Option[Boolean],
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
        check.doYouHaveUniqueTaxPayerReference(doYouHaveUniqueTaxPayerReference),
        check.confirmBusiness(confirmBusiness),
        check.nino(nino),
        check.whatIsYourName(whatIsYourName),
        check.whatIsYourDateOfBirth(whatIsYourDateOfBirth),
        check.whatAreYouRegisteringAs(whatAreYouRegisteringAs),
        check.businessWithoutIDName(businessWithoutIDName),
        check.addressUK(addressUK),
        check.doYouHaveNIN(doYouHaveNIN),
        check.nonUkName(nonUkName),
        check.doYouLiveInTheUK(doYouLiveInTheUK)
      ).flatten
  }

  case class FirstContact(contactName: Option[String], contactEmail: Option[String], contactPhone: Option[String])(implicit check: CheckYourAnswersHelper)
      extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(check.contactName(contactName), check.contactEmail(contactEmail), check.contactPhone(contactPhone)).flatten
  }

  case class SecondContact(secondContact: Option[Boolean], sndContactName: Option[String], sndContactEmail: Option[String], sndContactPhone: Option[String])(
    implicit check: CheckYourAnswersHelper
  ) extends AsRowSeq {

    def asRowSeq: Seq[SummaryList.Row] =
      Seq(check.secondContact(secondContact),
          check.sndContactName(sndContactName),
          check.sndContactEmail(sndContactEmail),
          check.sndContactPhone(sndContactPhone)
      ).flatten
  }
}
