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

package forms.mappings

import models.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

trait Formatters extends Transforms {

  private[mappings] def stringFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None | Some("") => Left(Seq(FormError(key, errorKey)))
        case Some(s)         => Right(s.trim)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey)))
          }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(requiredKey: String, wholeNumberKey: String, nonNumericKey: String, args: Seq[String] = Seq.empty): Formatter[Int] =
    new Formatter[Int] {

      val decimalCommaRegexp = """^-?(\d*[\.\,]\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right
          .flatMap {
            case s if s.matches(decimalCommaRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              nonFatalCatch
                .either(s.toInt)
                .left
                .map(
                  _ => Seq(FormError(key, nonNumericKey, args))
                )
          }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] val optionalStringFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      Right(
        data
          .get(key)
          .filter(_.lengthCompare(0) > 0)
      )

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private[mappings] val optionalMaxLengthStringFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      Right(
        data
          .get(key)
          .filter(_.lengthCompare(0) > 0)
      )

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String)(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).right.flatMap {
          str =>
            ev.withName(str).map(Right.apply).getOrElse(Left(Seq(FormError(key, invalidKey))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def stringTrimFormatter(errorKey: String, msgArg: String = ""): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None =>
          msgArg.isEmpty match {
            case true  => Left(Seq(FormError(key, errorKey)))
            case false => Left(Seq(FormError(key, errorKey, Seq(msgArg))))
          }
        case Some(s) =>
          s.trim match {
            case "" =>
              msgArg.isEmpty match {
                case true  => Left(Seq(FormError(key, errorKey)))
                case false => Left(Seq(FormError(key, errorKey, Seq(msgArg))))
              }
            case s1 => Right(s1)
          }
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  protected def validatedOptionalTextFormatter(invalidKey: String, lengthKey: String, regex: String, length: Int): Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
        data.get(key) match {
          case Some(str) if str.trim.length == 0 => Right(None)
          case Some(str) if !str.matches(regex)  => Left(Seq(FormError(key, invalidKey)))
          case Some(str) if str.length > length  => Left(Seq(FormError(key, lengthKey)))
          case Some(str)                         => Right(Some(str))
          case _                                 => Right(None)
        }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }

  protected def validatedOptionalTextAndMaxLengthFormatter(lengthKey: String, length: Int): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      data.get(key) match {
        case Some(str) if str.trim.length == 0 => Right(None)
        case Some(str) if str.length > length  => Left(Seq(FormError(key, lengthKey)))
        case Some(str)                         => Right(Some(str))
        case _                                 => Right(None)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  protected def validatedTextFormatter(requiredKey: String,
                                       invalidKey: String,
                                       lengthKey: String,
                                       regex: String,
                                       maxLength: Int,
                                       msgArg: String = ""
  ): Formatter[String] =
    new Formatter[String] {
      private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey, msgArg)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        dataFormatter
          .bind(key, data)
          .right
          .flatMap {
            case str if !str.matches(regex)    => Left(Seq(FormError(key, invalidKey)))
            case str if str.length > maxLength => Left(Seq(FormError(key, lengthKey)))
            case str                           => Right(str)
          }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  protected def textMaxLengthFormatter(requiredKey: String, lengthKey: String, maxLength: Int): Formatter[String] = new Formatter[String] {
    private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      dataFormatter
        .bind(key, data)
        .right
        .flatMap {
          case str if str.length > maxLength => Left(Seq(FormError(key, lengthKey)))
          case str                           => Right(str)
        }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  protected def requiredRegexOnly(requiredKey: String, invalidKey: String, validFormatRegex: String) = new Formatter[String] {
    private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      dataFormatter
        .bind(key, data)
        .right
        .flatMap {
          case str if !str.matches(validFormatRegex) => Left(Seq(FormError(key, invalidKey)))
          case str                                   => Right(str)
        }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  protected def maxLengthTextFormatter(requiredKey: String, lengthKey: String, maxLength: Int) = new Formatter[String] {
    private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      dataFormatter
        .bind(key, data)
        .right
        .flatMap {
          case str if str.length > maxLength => Left(Seq(FormError(key, lengthKey)))
          case str                           => Right(str)
        }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  protected def addressPostcodeFormatter(invalidKey: String, regex: String, emptyKey: String = "postCode.error.required"): Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val keydata = data.get(key)
        keydata match {
          case Some(s) if s.trim == "" =>
            Left(Seq(FormError(key, emptyKey)))
          case Some(s) if !stripSpaces(s).matches(regex) =>
            Left(Seq(FormError(key, invalidKey)))
          case Some(s) if stripSpaces(s).matches(regex) =>
            Right(Some(validPostCodeFormat(stripSpaces(s))))
          case _ => Left(Seq(FormError(key, emptyKey)))
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }

  private[mappings] def optionalPostcodeFormatter(requiredKey: String,
                                                  lengthKey: String,
                                                  invalidKey: String,
                                                  regex: String,
                                                  countryFieldName: String
  ): Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val postCode                        = postCodeDataTransform(data.get(key))
        val country                         = countryDataTransform(data.get(countryFieldName))
        val maxLengthPostcode               = 10
        val countryCodesThatRequirePostcode = List("JE", "GG", "IM")

        (postCode, country) match {
          case (Some(postcode), Some(countryCode)) if countryCodesThatRequirePostcode.contains(countryCode) && !stripSpaces(postcode).matches(regex) =>
            Left(Seq(FormError(key, invalidKey)))

          case (None, Some(countryCode)) if countryCodesThatRequirePostcode.contains(countryCode) => Left(Seq(FormError(key, requiredKey)))

          case (Some(postcode), _) if postcode.length <= maxLengthPostcode => Right(Some(postcode))

          case (Some(_), _) => Left(Seq(FormError(key, lengthKey)))

          case _ => Right(None)
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }

  protected def validatedFixedLengthTextFormatter(requiredKey: String, invalidKey: String, lengthKey: String, regex: String, length: Int, msgArg: String = "") =
    new Formatter[String] {

      private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey, msgArg)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        dataFormatter
          .bind(key, data)
          .right
          .flatMap {
            case str if !str.matches(regex) =>
              msgArg.isEmpty match {
                case true  => Left(Seq(FormError(key, invalidKey)))
                case false => Left(Seq(FormError(key, invalidKey, Seq(msgArg))))
              }
            case str if str.length != length =>
              msgArg.isEmpty match {
                case true  => Left(Seq(FormError(key, lengthKey)))
                case false => Left(Seq(FormError(key, lengthKey, Seq(msgArg))))
              }
            case str => Right(str)
          }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }
}
