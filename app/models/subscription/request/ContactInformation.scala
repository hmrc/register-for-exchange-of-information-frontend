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

package models.subscription.request

import models.WhatAreYouRegisteringAs.RegistrationTypeIndividual
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.{BusinessType, UserAnswers}
import pages._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._

import scala.language.implicitConversions

sealed trait ContactInformation

object ContactInformation {

  implicit lazy val reads: Reads[ContactInformation] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {
      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    OrganisationDetails.reads or
      IndividualDetails.reads
  }

  implicit val writes: Writes[ContactInformation] = Writes[ContactInformation] {
    case o: OrganisationDetails => Json.toJson(o)
    case i: IndividualDetails   => Json.toJson(i)
  }
}

case class OrganisationDetails(organisationName: String) extends ContactInformation

object OrganisationDetails {

  implicit lazy val reads: Reads[OrganisationDetails] = {
    import play.api.libs.functional.syntax._
    (__ \ "organisation" \ "organisationName").read[String] fmap OrganisationDetails.apply
  }

  implicit val writes: Writes[OrganisationDetails] =
    (__ \ "organisation" \ "organisationName").write[String] contramap unlift(OrganisationDetails.unapply)

  def convertTo(contactName: Option[String]): Option[OrganisationDetails] =
    contactName.map(OrganisationDetails(_))
}

case class IndividualDetails(firstName: String, middleName: Option[String], lastName: String) extends ContactInformation

object IndividualDetails {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[IndividualDetails] =
    (
      (__ \ "individual" \ "firstName").read[String] and
        (__ \ "individual" \ "middleName").readNullable[String] and
        (__ \ "individual" \ "lastName").read[String]
    )(IndividualDetails.apply _)

  implicit val writes: OWrites[IndividualDetails] =
    ((__ \ "individual" \ "firstName").write[String] and
      (__ \ "individual" \ "middleName").writeNullable[String] and
      (__ \ "individual" \ "lastName").write[String])(unlift(IndividualDetails.unapply))

  def convertTo(userAnswers: UserAnswers): Option[IndividualDetails] =
    (userAnswers.get(WhatIsYourNamePage), userAnswers.get(NonUkNamePage), userAnswers.get(SoleNamePage)) match {
      case (Some(name), _, _)           => Some(IndividualDetails(name.firstName, None, name.lastName))
      case (_, Some(nonUKName), _)      => Some(IndividualDetails(nonUKName.givenName, None, nonUKName.familyName))
      case (_, _, Some(soleTraderName)) => Some(IndividualDetails(soleTraderName.firstName, None, soleTraderName.lastName))
      case _                            => None
    }
}

case class PrimaryContact(contactInformation: ContactInformation, email: String, phone: Option[String], mobile: Option[String])

object PrimaryContact {

  implicit lazy val reads: Reads[PrimaryContact] = {
    import play.api.libs.functional.syntax._
    (
      __.read[ContactInformation] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(PrimaryContact.apply _)
  }

  implicit lazy val writes: OWrites[PrimaryContact] = {
    import play.api.libs.functional.syntax._
    (
      __.write[ContactInformation] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(PrimaryContact.unapply))
  }

  def convertTo(userAnswers: UserAnswers): Option[PrimaryContact] = {

    val contactNumber = userAnswers.get(ContactPhonePage)

    val individualOrSoleTrader = {
      (userAnswers.get(WhatAreYouRegisteringAsPage), userAnswers.get(BusinessTypePage)) match {
        case (Some(RegistrationTypeIndividual), _) => true
        case (_, Some(BusinessType.Sole))          => true
        case _                                     => false
      }
    }

    (for {
      email     <- userAnswers.get(ContactEmailPage)
      havePhone <- userAnswers.get(IsContactTelephonePage)
      contactInformation <-
        if (individualOrSoleTrader) {
          IndividualDetails.convertTo(userAnswers)
        } else {
          OrganisationDetails.convertTo(userAnswers.get(ContactNamePage))
        }
    } yield
      if (havePhone && userAnswers.get(ContactPhonePage).isEmpty) {
        None
      } else {
        Some(PrimaryContact(contactInformation = contactInformation, email = email, phone = contactNumber, mobile = None))
      }).flatten

  }
}

case class SecondaryContact(contactInformation: ContactInformation, email: String, phone: Option[String], mobile: Option[String])

object SecondaryContact {

  implicit lazy val reads: Reads[SecondaryContact] = {
    import play.api.libs.functional.syntax._
    (
      __.read[ContactInformation] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(SecondaryContact.apply _)
  }

  implicit lazy val writes: OWrites[SecondaryContact] = {
    import play.api.libs.functional.syntax._
    (
      __.write[ContactInformation] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(SecondaryContact.unapply))
  }

  def convertTo(userAnswers: UserAnswers): Either[ApiError, Option[SecondaryContact]] = {
    val secondaryContactNumber = userAnswers.get(SndContactPhonePage)

    (userAnswers.get(SecondContactPage), userAnswers.get(SndConHavePhonePage)) match {
      case (Some(true), Some(true)) if secondaryContactNumber.isDefined =>
        Right(secondaryContact(userAnswers, secondaryContactNumber))
      case (Some(true), Some(false)) =>
        Right(secondaryContact(userAnswers, None))
      case (Some(false), _) => Right(None)
      case _                => Left(MandatoryInformationMissingError)
    }

  }

  private def secondaryContact(userAnswers: UserAnswers, secondaryContactNumber: Option[String]) =
    for {
      orgDetails     <- OrganisationDetails.convertTo(userAnswers.get(SndContactNamePage))
      secondaryEmail <- userAnswers.get(SndContactEmailPage)
    } yield SecondaryContact(contactInformation = orgDetails, email = secondaryEmail, phone = secondaryContactNumber, mobile = None)
}
