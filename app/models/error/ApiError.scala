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

package models.error

import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpReads.{is4xx, is5xx}

sealed trait ApiError

object ApiError {

  implicit def readEitherOf[A: HttpReads]: HttpReads[Either[ApiError, A]] =
    HttpReads.ask.flatMap {
      case (_, _, response) =>
        response.status match {
          case status if status == 404 => HttpReads.pure(Left(NotFoundError))
          case status if is4xx(status) => HttpReads.pure(Left(BadRequestError))
          case status if is5xx(status) => HttpReads.pure(Left(ServiceUnavailableError))
          case _                       => HttpReads[A].map(Right.apply)
        }
    }

  def toError(errorDetail: ErrorDetail): ApiError = errorDetail match {
    case ErrorDetail(_, _, "404", _, _, _) => NotFoundError
    case _                                 => ServiceUnavailableError
  }

  case object BadRequestError extends ApiError

  case object NotFoundError extends ApiError

  case object ServiceUnavailableError extends ApiError

  case object MandatoryInformationMissingError extends ApiError

  case object DuplicateSubmissionError extends ApiError

  case object UnableToCreateEMTPSubscriptionError extends ApiError
}