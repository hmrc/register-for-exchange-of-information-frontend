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
import helpers.JsonFixtures.{registerWithIDJson, registerWithIDPayloadJson}
import models.IdentifierType.NINO
import models.Regime.MDR
import models.register.request.details.WithIDIndividual
import models.register.request.{RegisterWithID, RegisterWithIDRequest, RequestCommon, RequestWithIDDetails}
import models.shared.Parameters
import play.api.libs.json.Json

import scala.util.matching.Regex

class RegisterWithIDRequestSpec extends SpecBase {

  val registrationWithIDPayload: RegisterWithID = RegisterWithID(
    RegisterWithIDRequest(
      RequestCommon(
        "2016-08-16T15:55:30Z",
        MDR.toString,
        "ec031b045855445e96f98a569ds56cd2",
        Some(Seq(Parameters("REGIME", MDR.toString)))
      ),
      RequestWithIDDetails(
        NINO,
        TestNiNumber,
        requiresNameMatch = true,
        isAnAgent = false,
        Option(WithIDIndividual(name.firstName, Some(MiddleName), name.lastName, Some(TestDate)))
      )
    )
  )

  "RegisterWithIDRequest" - {
    "marshal from Json Registration with ID" in {
      Json.parse(registerWithIDPayloadJson).validate[RegisterWithID].get mustBe registrationWithIDPayload
    }

    "marshal to json" in {
      Json.toJson(registrationWithIDPayload) mustBe registerWithIDJson
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
