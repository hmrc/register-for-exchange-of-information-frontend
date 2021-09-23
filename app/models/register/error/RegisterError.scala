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

package models.register.error

sealed trait RegisterError {
  val errorCode: String
  val errorMessage: String
}

object RegisterError {

  def toError(errorDetail: ErrorDetail): RegisterError = errorDetail match {
    case ErrorDetail(_, _, "503", _, _, SourceDetail(detail)) if detail.contains("001 - Request could not be processed") =>
      RequestCouldNotBeProcessedError
    case ErrorDetail(_, _, "409", "Duplicate submission", _, _) =>
      DuplicateSubmissionError
    case _ => DuplicateSubmissionError //UnableToParseError
  }

  case object BadRequestError extends RegisterError {
    val errorCode: String    = "400"
    val errorMessage: String = "Invalid ID"
  }

  case object RecordNotFoundError extends RegisterError {
    val errorCode: String    = "404"
    val errorMessage: String = "Record not Found"
  }

  case object DuplicateSubmissionError extends RegisterError {
    val errorCode: String    = "409"
    val errorMessage: String = "Duplicate submission"
  }

  case object InternalServerError extends RegisterError {
    val errorCode: String    = "500"
    val errorMessage: String = "InternalError"
  }

  case object RequestCouldNotBeProcessedError extends RegisterError {
    val errorCode: String    = "503"
    val errorMessage: String = "Request could not be processed"
  }

  case object ServiceUnavailableError extends RegisterError {
    val errorCode: String    = "503"
    val errorMessage: String = "Send timeout"
  }

  case object UnableToParseError extends RegisterError {
    val errorCode: String    = ""
    val errorMessage: String = "There has been an error"
  }

  case object UnableToCreateRegistrationError extends RegisterError {
    val errorCode: String    = ""
    val errorMessage: String = "Couldn't Create Payload for Register With ID"
  }

  case object MissingNinoAnswerError extends RegisterError {
    val errorCode: String    = ""
    val errorMessage: String = "Missing Nino Answer"
  }

  case object UnableToCreateEMTPSubscriptionError extends RegisterError {
    val errorCode: String    = ""
    val errorMessage: String = "Unable to create an ETMP subscription"
  }

  case object SomeInformationIsMissingError extends RegisterError {
    val errorCode: String    = ""
    val errorMessage: String = "Some information is missing"
  }
}
