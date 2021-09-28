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

package models.register

import models.UserAnswers
import models.register.request.RegisterWithIDRequest
import play.api.libs.json.{Format, Json}

case class PayloadRegisterWithID(registerWithIDRequest: RegisterWithIDRequest)

object PayloadRegisterWithID {
  implicit val format: Format[PayloadRegisterWithID] = Json.format[PayloadRegisterWithID]

  def createIndividualSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[PayloadRegisterWithID] =
    for {
      individual <- RequestWithIDDetails.createIndividualSubmission(userAnswers, identifierName, identifierValue)
    } yield PayloadRegisterWithID(
      request.RegisterWithIDRequest(
        RequestCommon.forService,
        individual
      )
    )

//  def createBusinessSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[PayloadRegisterWithID] =
//    for {
//      request <- RequestWithIDDetails.createBusinessSubmission(userAnswers, identifierName, identifierValue)
//    } yield PayloadRegisterWithID(
//      RegisterWithIDRequest(
//        RequestCommon.forService,
//        request
//      )
//    )
}
