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

package generators

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Gen, Shrink}
import utils.RegexConstants
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.{Instant, LocalDate, ZoneOffset}

trait Generators extends UserAnswersGenerator with PageGenerators with ModelGenerators with UserAnswersEntryGenerators with RegexConstants {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  val one           = 1
  val two           = 2
  val nine          = 9
  val ten           = 10
  val zero          = 0
  val fifty         = 50
  val oneHundred    = 100
  val twentyFour    = 24
  val listOfNumbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9)

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield seq1.toSeq.zip(seq2).foldLeft("") {
      case (acc, (n, Some(v))) =>
        acc + n + v
      case (acc, (n, _)) =>
        acc + n
    }
  }

  def stringsLongerThanAlpha(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * two).max(oneHundred)
    length    <- Gen.chooseNum(minLength + one, maxLength)
    chars     <- listOfN(length, Gen.alphaChar)
  } yield chars.mkString

  def validAddressLine: Gen[String] = RegexpGen.from(apiAddressRegex)

  def validOrganisationName(maxLength: Int): Gen[String] = for {
    length <- Gen.chooseNum(maxLength, maxLength)
    chars  <- listOfN(length, RegexpGen.from(orgNameRegex))
  } yield chars.mkString

  def stringWithinMaxLengthByRegex(maxLength: Int, regex: String): Gen[String] = for {
    length <- Gen.chooseNum(one, maxLength)
    chars  <- listOfN(length, RegexpGen.from(regex))
  } yield chars.mkString match {
    case str if str.isEmpty            => str
    case str if str.length > maxLength => str.substring(zero, maxLength - one)
    case str                           => str
  }

  def stringWithFixedLengthByRegex(length: Int, regex: String): Gen[String] = {
    for {
      chars <- listOfN(length, RegexpGen.from(regex))
    } yield chars.mkString match {
      case str if str.isEmpty         => str
      case str if str.length > length => str.substring(zero, length - one)
      case str                        => str
    }
  } suchThat (_.length == length)

  def validUtr: Gen[String] = for {
    chars <- listOfN(ten, Gen.oneOf(listOfNumbers))
  } yield chars.mkString

  def nonEmptyStringWithinMaxLengthByRegex(maxLength: Int, regex: String): Gen[String] = stringWithinMaxLengthByRegex(maxLength, regex) suchThat (_.nonEmpty)

  def validPersonalName(maxLength: Int): Gen[String] = RegexpGen.from(individualNameRegex) suchThat (_.length > maxLength)

  def validEmailAddress: Gen[String] = RegexpGen.from(emailRegex)

  def validEmailAddressToLong(maxLength: Int): Gen[String] =
    for {
      part <- listOfN(maxLength, Gen.alphaChar).map(_.mkString)

    } yield s"$part.$part@$part.$part"

  def validNino: Gen[String] = for {
    first   <- Gen.oneOf("ACEHJLMOPRSWXY".toCharArray)
    second  <- Gen.oneOf("ABCEGHJKLMNPRSTWXYZ".toCharArray)
    numbers <- listOfN(6, Gen.oneOf(listOfNumbers))
    last    <- Gen.oneOf("ABCD".toCharArray)
  } yield s"$first$second${numbers.mkString}$last"

  val subscriptionIDRegex              = "^[X][A-Z][0-9]{13}"
  def validSubscriptionID: Gen[String] = RegexpGen.from(subscriptionIDRegex)

  val safeIDRegex              = "^[0-9A-Za-z]{1,15}"
  def validSafeID: Gen[String] = RegexpGen.from(safeIDRegex)

  def validPostCodes: Gen[String] = {
    val disallowed = List('c', 'i', 'k', 'm', 'o', 'v')
    for {
      pt1Quantity <- Gen.choose(one, two)
      pt1         <- Gen.listOfN(pt1Quantity, Gen.alphaChar).map(_.mkString)
      pt2         <- Gen.choose(zero, nine)

      pt3alphaOpt <- Gen.option(Gen.alphaChar)
      pt3numOpt   <- Gen.option(Gen.choose(zero, nine))
      pt3 = if (pt3alphaOpt.isEmpty) pt3numOpt.getOrElse("").toString else pt3alphaOpt.get.toString

      pt4 <- Gen.choose(zero, nine)
      pt5a <- Gen.alphaChar suchThat (
        ch => !disallowed.contains(ch.toLower)
      )
      pt5b <- Gen.alphaChar suchThat (
        ch => !disallowed.contains(ch.toLower)
      )
    } yield s"$pt1$pt2$pt3 $pt4$pt5a$pt5b"
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x > Int.MaxValue
    )

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x < Int.MinValue
    )

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > zero)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map(_.formatted("%f"))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (
      x => x < min || x > max
    )

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * two).max(oneHundred)
    length    <- Gen.chooseNum(minLength + one, maxLength)
    chars     <- listOfN(length, arbitrary[Char])
  } yield chars.mkString

  def phoneMaxLength(ln: Int): Gen[String] = for {
    length <- Gen.chooseNum(ln, twentyFour)
    chars  <- listOfN(length, Gen.chooseNum(zero, nine))
  } yield "+" + chars.mkString

  def validPhoneNumber(ln: Int): Gen[String] = for {
    length <- Gen.chooseNum(one, ln - one)
    chars  <- listOfN(length, Gen.chooseNum(zero, nine))
  } yield "+" + chars.mkString

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(zero, vector.size - one).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def stringsNotOfFixedLengthNumeric(givenLength: Int): Gen[String] = for {
    maxLength <- givenLength + fifty
    length    <- Gen.chooseNum(one, maxLength).suchThat(_ != givenLength)
    chars     <- listOfN(length, Gen.numChar)
  } yield chars.mkString

  def validEmailAddressService: Gen[String] = {
    val emailRegexWithQuantifier = """^([a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$"""

    RegexpGen.from(emailRegexWithQuantifier)
  }
}
