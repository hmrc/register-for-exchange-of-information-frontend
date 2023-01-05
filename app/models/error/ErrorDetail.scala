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

package models.error

import play.api.libs.json.{Json, OFormat}

case class SourceDetail(detail: Seq[String])

object SourceDetail {
  implicit val format: OFormat[SourceDetail] = Json.format[SourceDetail]
}

case class ErrorDetail(
  timestamp: String,
  correlationId: Option[String],
  errorCode: String,
  errorMessage: String,
  source: String,
  sourceFaultDetail: SourceDetail
)

object ErrorDetail {
  implicit val format: OFormat[ErrorDetail] = Json.format[ErrorDetail]
}

case class ErrorResponse(errorDetail: ErrorDetail)

object ErrorResponse {
  implicit val format: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}
