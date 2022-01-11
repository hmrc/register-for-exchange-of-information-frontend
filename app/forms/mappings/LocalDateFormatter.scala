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

import models.DateHelper.formatDateToString
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey: String,
  allRequiredKey: String,
  twoRequiredKey: String,
  requiredKey: String,
  maxDateKey: String,
  minDateKey: String,
  maxDate: LocalDate,
  minDate: LocalDate,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalDate]
    with Formatters {

  private val fieldKeys: List[String] = List("day", "month", "year")

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(returnKey(key, "day"), invalidKey, args)))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day   <- int.bind(s"$key.day", data).right
      month <- int.bind(s"$key.month", data).right
      year  <- int.bind(s"$key.year", data).right
      date  <- toDate(key, day, month, year).right
    } yield date
  }

  val returnKey: (String, String) => String = (key: String, fieldName: String) => s"$key.$fieldName"

  private def validateDay(day: String): Boolean = Try(day.toInt) match {
    case Success(datefield) => datefield >= 1 && datefield <= 31
    case Failure(_)         => false
  }

  private def validateMonth(month: String): Boolean = Try(month.toInt) match {
    case Success(datefield) => datefield >= 1 && datefield <= 12
    case Failure(_)         => false
  }

  private def validateYear(year: String): Boolean = Try(year.toInt) match {
    case Success(_) => true
    case Failure(_) => false
  }

  private def validateDateField(field: String, value: String): Option[String] =
    field match {
      case fieldValue if fieldValue.matches(""".*[.]day$""")   => if (validateDay(value)) None else Some("day")
      case fieldValue if fieldValue.matches(""".*[.]month$""") => if (validateMonth(value)) None else Some("month")
      case fieldValue if fieldValue.matches(""".*[.]year$""")  => if (validateYear(value)) None else Some("year")
    }

  private def missingFields(key: String, data: Map[String, String]): Either[Seq[FormError], Map[String, String]] = {

    def messageNeeded(numberOfFields: Int) =
      numberOfFields match {
        case 3 => allRequiredKey
        case 2 => twoRequiredKey
        case 1 => requiredKey
      }

    val missingFields = fieldKeys flatMap {
      field => if (!data.contains(s"$key.$field") || data(s"$key.$field").isEmpty) Some(field) else None
    }

    if (missingFields.isEmpty) {
      Right(data)
    } else {
      if (fieldKeys.size == missingFields.size) {
        Left(Seq(FormError(returnKey(key, missingFields.head), messageNeeded(missingFields.size), args)))
      } else {
        Left(Seq(FormError(returnKey(key, missingFields.head), messageNeeded(missingFields.size), missingFields ++ args)))
      }
    }
  }

  private def validNumbers(key: String, data: Map[String, String]): Either[Seq[FormError], Map[String, String]] =
    data.toList
      .filter(_._1 != "csrfToken")
      .flatMap(
        (dateField: (String, String)) => validateDateField(dateField._1, dateField._2)
      ) match {
      case Nil                      => Right(data)
      case items if items.size == 1 => Left(Seq(FormError(returnKey(key, items.head), invalidKey, items ++ args)))
      case _                        => Left(Seq(FormError(returnKey(key, "day"), invalidKey, args)))
    }

  private def datePredicate(key: String, date: LocalDate, testDate: LocalDate, message: String)(f: LocalDate => Boolean): Either[Seq[FormError], LocalDate] =
    if (f(date)) Left(Seq(FormError(returnKey(key, "day"), message, List(formatDateToString(testDate)) ++ args))) else Right(date)

  private def trimMap(data: Map[String, String]) =
    data.foldLeft(Map.empty[String, String])(
      (accum, values) => accum + (values._1 -> values._2.trim)
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    for {
      missing    <- missingFields(key, trimMap(data))
      valid      <- validNumbers(key, missing)
      dateValid  <- formatDate(key, valid)
      dateBefore <- datePredicate(key, dateValid, minDate, minDateKey)(_.isBefore(minDate))
      dateAfter  <- datePredicate(key, dateBefore, maxDate, maxDateKey)(_.isAfter(maxDate))
    } yield dateAfter

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )
}
