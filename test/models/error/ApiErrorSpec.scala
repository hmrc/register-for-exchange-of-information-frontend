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

import base.SpecBase
import models.enrolment.GroupIds
import play.api.http.Status._
import play.api.libs.json.JsString
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext

class ApiErrorSpec extends SpecBase {

  import ApiError._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "ApiError" - {

    "convertToErrorCode" - {

      "must return the correct status code for each ApiError" in {
        convertToErrorCode(BadRequestError) mustBe BAD_REQUEST
        convertToErrorCode(NotFoundError) mustBe NOT_FOUND
        convertToErrorCode(ServiceUnavailableError) mustBe SERVICE_UNAVAILABLE
        convertToErrorCode(InternalServerError) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(MandatoryInformationMissingError()) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(DuplicateSubmissionError) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(UnableToCreateEMTPSubscriptionError) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(UnableToCreateEnrolmentError) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(
          EnrolmentExistsError(GroupIds(Seq("groupId1"), Seq("groupId2")))
        ) mustBe INTERNAL_SERVER_ERROR
        convertToErrorCode(MalformedError(500)) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "readEitherOf" - {

      "must return Left(NotFoundError) for 404 status" in {
        val result: Either[ApiError, String] = readEitherOf[String].read("", "", HttpResponse(NOT_FOUND, ""))
        result mustBe Left(NotFoundError)
      }

      "must return Left(BadRequestError) for 4xx status" in {
        val result: Either[ApiError, String] = readEitherOf[String].read("", "", HttpResponse(BAD_REQUEST, ""))
        result mustBe Left(BadRequestError)
      }

      "must return Left(ServiceUnavailableError) for SERVICE_UNAVAILABLE status" in {
        val result: Either[ApiError, String] = readEitherOf[String].read("", "", HttpResponse(SERVICE_UNAVAILABLE, ""))
        result mustBe Left(ServiceUnavailableError)
      }

      "must return Left(InternalServerError) for 5xx status" in {
        val result: Either[ApiError, String] =
          readEitherOf[String].read("", "", HttpResponse(INTERNAL_SERVER_ERROR, ""))
        result mustBe Left(InternalServerError)
      }

      "must return Right(data) for a successful response" in {
        val result: Either[ApiError, String] =
          readEitherOf[String].read("", "", HttpResponse(OK, JsString("data"), Map.empty))
        result mustBe Right("data")
      }
    }
  }
}
