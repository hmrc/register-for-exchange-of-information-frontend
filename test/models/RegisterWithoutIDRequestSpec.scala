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

package models

import base.SpecBase
import helpers.JsonFixtures.{registerWithoutIDJson, registerWithoutIDPayloadJson}
import models.Regime.MDR
import models.register.request._
import models.register.request.details.{AddressRequest, Identification, Individual}
import models.shared.{ContactDetails, Parameters}
import play.api.libs.json.Json

import java.time.LocalDate
import scala.util.matching.Regex

class RegisterWithoutIDRequestSpec extends SpecBase {

  val individual = Individual(name = name, dateOfBirth = LocalDate.parse(TestDate))

  val aaddress = AddressRequest(addressLine1 = "line 1",
                                addressLine2 = Some("line 2"),
                                addressLine3 = "line 3",
                                addressLine4 = Some("line 4"),
                                postalCode = Some("SW1A 2BQ"),
                                countryCode = "GB"
  )

  val contactDetails = ContactDetails(
    phoneNumber = Some("111111"),
    mobileNumber = Some("222222"),
    faxNumber = Some("333333"),
    emailAddress = Some(TestEmail)
  )

  val identification = Identification(idNumber = "", issuingInstitution = "", issuingCountryCode = "")

  val registerWithoutIDPayload: RegisterWithoutID = RegisterWithoutID(
    RegisterWithoutIDRequest(
      RequestCommon("2016-08-16T15:55:30Z", MDR.toString, "ec031b045855445e96f98a569ds56cd2", Some(Seq(Parameters("REGIME", MDR.toString)))),
      RequestWithoutIDDetails(
        individual = Some(individual),
        organisation = None,
        address = aaddress,
        contactDetails = contactDetails,
        identification = Some(identification)
      )
    )
  )

  "RegisterWithoutIDRequest" - {
    "marshal from Json Registration without ID" in {
      Json.parse(registerWithoutIDPayloadJson).validate[RegisterWithoutID].get mustBe registerWithoutIDPayload
    }

    "marshal to json" in {
      Json.toJson(registerWithoutIDPayload) mustBe registerWithoutIDJson
    }

    "response common must generate correct values to spec" in {
      val requestCommon = RequestCommon(MDR.toString)

      val ackRefLength = requestCommon.acknowledgementReference.length
      ackRefLength >= 1 && ackRefLength <= 32 mustBe true

      requestCommon.regime mustBe MDR.toString

      val date: Regex = raw"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z".r
      date.findAllIn(requestCommon.receiptDate).toList.nonEmpty mustBe true
    }
  }
}
