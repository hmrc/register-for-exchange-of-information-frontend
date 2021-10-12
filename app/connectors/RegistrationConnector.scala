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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import models.register.error.ApiError.{BadRequest, NotFoundError, ServiceUnavailableError}
import models.register.error.{ApiError, RegisterWithIDErrorResponse}
import models.register.request.RegisterWithID
import models.register.response.RegistrationWithIDResponse
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) {

  private val logger: Logger = Logger(this.getClass)

  val submissionUrl = s"${config.businessMatchingUrl}/registration"

  def registerWithID(
    registration: RegisterWithID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, RegistrationWithIDResponse] =
    EitherT {
      http.POST[RegisterWithID, HttpResponse](s"$submissionUrl/individual/nino", registration) map {
        case responseMessage if is2xx(responseMessage.status) =>
          Right(responseMessage.json.as[RegistrationWithIDResponse])
        case responseMessage if responseMessage.status == NOT_FOUND =>
          logger.error("Error in individual registration with nino: not found.")
          Left(NotFoundError)
        case responseMessage if responseMessage.status == BAD_REQUEST =>
          logger.error("Error in individual registration with nino: invalid.")
          Left(BadRequest)
        case responseMessage =>
          responseMessage.json.validate[RegisterWithIDErrorResponse] match {
            case JsSuccess(response, _) =>
              val errorDetail = response.errorDetail
              logger.error(s"Error in individual registration with nino: $errorDetail.")
              Left(ApiError.toError(errorDetail))
            case JsError(errors) =>
              logger.error(s"Error in individual registration with nino: $errors.")
              Left(ServiceUnavailableError)
          }
      }
    }
}
