/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import connectors.EmailConnector
import models.email.{EmailRequest, EmailTemplate, EmailUserType}
import models.error.ApiError
import models.{SubscriptionID, UserAnswers}
import pages._
import play.api.Logging
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector, emailTemplate: EmailTemplate, userType: EmailUserType)(implicit
  executionContext: ExecutionContext
) extends Logging {

  def sendAnLogEmail(userAnswers: UserAnswers, subscriptionID: SubscriptionID)(implicit hc: HeaderCarrier): Future[Either[ApiError, Int]] =
    sendEmail(userAnswers, subscriptionID) map {
      case Some(resp) =>
        resp.status match {
          case NOT_FOUND   => logger.warn("The template cannot be found within the email service")
          case BAD_REQUEST => logger.warn("Missing email or name parameter")
          case ACCEPTED    => logger.info("Email queued")
          case _           => logger.warn(s"Unhandled status received from email service ${resp.status}")
        }
        Right(resp.status)
      case _ =>
        logger.warn("Failed to send email")
        Right(INTERNAL_SERVER_ERROR)
    }

  def sendEmail(userAnswers: UserAnswers, subscriptionID: SubscriptionID)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] = {

    val emailAddress = userAnswers.get(ContactEmailPage)

    val contactName =
      (userAnswers.get(ContactNamePage).isDefined,
       userAnswers.get(WhatIsYourNamePage).isDefined,
       userAnswers.get(NonUkNamePage).isDefined,
       userAnswers.get(SoleNamePage).isDefined
      ) match {
        case (true, false, false, false) =>
          userAnswers
            .get(ContactNamePage)
            .map(
              n => n
            )
        case (false, true, false, false) =>
          userAnswers
            .get(WhatIsYourNamePage)
            .map(
              n => s"${n.firstName} ${n.lastName}"
            )
        case (false, false, true, false) =>
          userAnswers
            .get(NonUkNamePage)
            .map(
              n => s"${n.givenName} ${n.familyName}"
            )
        case (false, false, false, true) =>
          userAnswers
            .get(SoleNamePage)
            .map(
              n => s"${n.firstName} ${n.lastName}"
            )
        case _ => None
      }

    val secondaryEmailAddress = userAnswers.get(SndContactEmailPage)
    val secondaryName         = userAnswers.get(SndContactNamePage)

    for {

      primaryResponse <- emailAddress
        .filter(EmailAddress.isValid)
        .fold(Future.successful(Option.empty[HttpResponse])) {
          email =>
            emailConnector
              .sendEmail(
                EmailRequest.mdrRegistration(email, contactName, emailTemplate.getTempate(userType.getUserTypeFromUa(userAnswers)), subscriptionID.value)
              )
              .map(Some.apply)
        }

      _ <- secondaryEmailAddress
        .filter(EmailAddress.isValid)
        .fold(Future.successful(Option.empty[HttpResponse])) {
          secondaryEmailAddress =>
            emailConnector
              .sendEmail(
                EmailRequest
                  .mdrRegistration(secondaryEmailAddress,
                                   secondaryName,
                                   emailTemplate.getTempate(userType.getUserTypeFromUa(userAnswers)),
                                   subscriptionID.value
                  )
              )
              .map(Some.apply)
        }
    } yield primaryResponse

  }
}
