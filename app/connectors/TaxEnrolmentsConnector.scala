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
import models.enrolment.SubscriptionInfo
import models.error.ApiError
import models.error.ApiError.{BadRequestError, NotFoundError, ServiceUnavailableError}
import models.register.response.RegistrationWithIDResponse
import models.subscription.response.SubscriptionID
import play.api.Logger
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentsConnector @Inject() (
  val config: FrontendAppConfig,
  val http: HttpClient
) {

  private val logger: Logger = Logger(this.getClass)

  def createEnrolment(
    enrolmentInfo: SubscriptionInfo
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, Int, Int] = {

    val url = s"${config.taxEnrolmentsUrl}"

    val json = Json.toJson(enrolmentInfo.convertToEnrolmentRequest)

    EitherT {
      http.PUT[JsValue, HttpResponse](url, json) map {
        case responseMessage if is2xx(responseMessage.status) =>
          Right(responseMessage.status)
        case responseMessage =>
          logger.error(s"Error with tax-enrolments call  ${responseMessage.status} : ${responseMessage.body}")
          Left(responseMessage.status)
      }
    }
  }
}
