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

package services

import connectors.EmailConnector
import models.email.{EmailRequest, EmailTemplate, EmailUserType}
import models.{SubscriptionID, UserAnswers}
import pages._
import play.api.Logging
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector, emailTemplate: EmailTemplate, userType: EmailUserType)(
  implicit executionContext: ExecutionContext
) extends Logging {

  def sendAnLogEmail(userAnswers: UserAnswers, subscriptionID: SubscriptionID)(implicit
    hc: HeaderCarrier
  ): Future[Int] =
    sendEmail(userAnswers, subscriptionID) map {
      case Some(resp) =>
        resp.status match {
          case NOT_FOUND   => logger.warn("The template cannot be found within the email service")
          case BAD_REQUEST => logger.warn("Missing email or name parameter")
          case ACCEPTED    => logger.info("Email queued")
          case _           => logger.warn(s"Unhandled status received from email service ${resp.status}")
        }
        resp.status
      case _          =>
        logger.warn("The email could not be sent to the EMAIL service")
        INTERNAL_SERVER_ERROR
    }

  private def getContactName(userAnswers: UserAnswers) = (
    userAnswers.get(ContactNamePage).isDefined,
    userAnswers.get(WhatIsYourNamePage).isDefined,
    userAnswers.get(NonUkNamePage).isDefined,
    userAnswers.get(SoleNamePage).isDefined
  ) match {
    case (true, false, false, false) =>
      userAnswers
        .get(ContactNamePage)
        .map(n => n)
    case (false, true, false, false) =>
      userAnswers
        .get(WhatIsYourNamePage)
        .map(n => s"${n.firstName} ${n.lastName}")
    case (false, false, true, false) =>
      userAnswers
        .get(NonUkNamePage)
        .map(n => s"${n.givenName} ${n.familyName}")
    case (false, false, false, true) =>
      userAnswers
        .get(SoleNamePage)
        .map(n => s"${n.firstName} ${n.lastName}")
    case _                           => None
  }

  private def getEmail(userAnswers: UserAnswers) =
    userAnswers.get(ContactEmailPage).orElse(userAnswers.get(IndividualContactEmailPage))

  def sendEmail(userAnswers: UserAnswers, subscriptionID: SubscriptionID)(implicit
    hc: HeaderCarrier
  ): Future[Option[HttpResponse]] =
    for {

      primaryResponse <- getEmail(userAnswers)
                           .filter(EmailAddress.isValid)
                           .fold(Future.successful(Option.empty[HttpResponse])) { email =>
                             emailConnector
                               .sendEmail(
                                 EmailRequest
                                   .mdrRegistration(
                                     email,
                                     getContactName(userAnswers),
                                     emailTemplate.getTempate(userType.getUserTypeFromUa(userAnswers)),
                                     subscriptionID.value
                                   )
                               )
                               .map(Some.apply)
                           }

      _ <- userAnswers
             .get(SndContactEmailPage)
             .filter(EmailAddress.isValid)
             .fold(Future.successful(Option.empty[HttpResponse])) { secondaryEmailAddress =>
               emailConnector
                 .sendEmail(
                   EmailRequest
                     .mdrRegistration(
                       secondaryEmailAddress,
                       userAnswers.get(SndContactNamePage),
                       emailTemplate.getTempate(userType.getUserTypeFromUa(userAnswers)),
                       subscriptionID.value
                     )
                 )
                 .map(Some.apply)
             }
    } yield primaryResponse

}

object EmailAddress {
  val validEmail = """^([a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$""".r

  def isValid(email: String) = email match {
    case validEmail(_, _) => true
    case _                => false
  }

}
