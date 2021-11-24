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

package controllers

import cats.data.EitherT
import cats.implicits._
import models.UserAnswers
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.requests.DataRequest
import pages.QuestionPage
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.AnyContent
import queries.Gettable

import scala.concurrent.{ExecutionContext, Future}

trait WithEitherT {

  def getEither[A](page: Gettable[A])(implicit ec: ExecutionContext, request: DataRequest[AnyContent], reads: Reads[A]): EitherT[Future, ApiError, A] =
    EitherT.fromEither {
      request.userAnswers
        .get(page)
        .toRight(MandatoryInformationMissingError(page.toString))
    }

  def getEither[A](value: Option[A], msg: String)(implicit
    ec: ExecutionContext,
    request: DataRequest[AnyContent],
    reads: Reads[A]
  ): EitherT[Future, ApiError, A] =
    EitherT.fromOption(value, MandatoryInformationMissingError(msg))

  def setEither[A](page: QuestionPage[A],
                   value: A,
                   checkPrevious: Boolean = false
  )(implicit ec: ExecutionContext, request: DataRequest[AnyContent], writes: Writes[A], reads: Reads[A]): EitherT[Future, ApiError, UserAnswers] =
    EitherT.fromEither {
      request.userAnswers
        .setB(page, value, checkPrevious)
        .toOption
        .toRight(MandatoryInformationMissingError(page.toString))
    }

}
