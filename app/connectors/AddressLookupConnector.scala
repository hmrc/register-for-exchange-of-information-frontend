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

import config.FrontendAppConfig
import models.{AddressLookup, LookupAddressByPostcode}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddressLookupConnector @Inject() (http: HttpClient, config: FrontendAppConfig) {
  private val logger: Logger = Logger(this.getClass)

  def addressLookupByPostcode(postCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[AddressLookup]] = {

    val addressLookupUrl: String = s"${config.addressLookUpUrl}/lookup"

    implicit val reads: Reads[Seq[AddressLookup]] = AddressLookup.addressesLookupReads

    val lookupAddressByPostcode = LookupAddressByPostcode(postCode, None)

    http.POST[LookupAddressByPostcode, HttpResponse](addressLookupUrl, lookupAddressByPostcode, headers = Seq("X-Hmrc-Origin" -> "DAC6")) flatMap {
      case response if response.status equals OK =>
        val adrBase = response.json
          .as[Seq[AddressLookup]]
          .filterNot(
            address => address.addressLine1.isEmpty && address.addressLine2.isEmpty
          )

        //Future.successful(sortAddresses(adrBase))
        Future.successful(sort_v2(adrBase))
      case response =>
        val message = s"Address Lookup failed with status ${response.status} Response body: ${response.body}"
        Future.failed(new HttpException(message, response.status))
    } recover {
      case e: Exception =>
        logger.error("Exception in Address Lookup", e)
        throw e
    }
  }

  private def sortAddresses(items: Seq[AddressLookup]): Seq[AddressLookup] = {

    // todo hmm dodac 0 na koncu dla kazdego int aby nie wywalalo Ex i wtedy zobacze co jest
    /*
     szkola mail
        - pedagog mail
        - logoda kontakt
        - klinika psychologiczna badania kontakt
        - alus nauczycielka kontakt
        - jimi nauczycielka kontakt

    - QA as stagging
    - deploy branch to qa
    - kibana -> logs -> Jenkins login


    https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in

    /register-for-exchange-of-information/register/mdr/have-utr

    NE3 4ET


    Efer House 137a, Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
    99-99a, Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
    135 Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
    141 Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
    143 Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
    153 Back High Street, Gosforth, Newcastle upon Tyne, NE3 4ET
     */
    logger.info(s"\n\nDEBUG_MSGS\nitems=${items.toString()}\n\n")
    logger.debug(s"\n\nDEBUG_MSGS\nitems=${items.toString()}\n\n")

    items
      // group (gr_key, INT, adrs)
      .map(
        item =>
          (item.addressLine2 match {
             case Some(x) => x
             case None    => (item.addressLine1.getOrElse(""))
           },
           item.addressLine1.map(
             b => ("""\d+""".r findAllIn b).toList.mkString.concat("0").toInt // todo del 0 concat
           ), // int from address_1
           item // whole address
          )
      )
      .groupBy(_._1)
      .toSeq
      // sort groups
      .sortBy(
        x => ("""\d+""".r findAllIn x._1).toList.mkString.concat("0").toInt // todo del 0 concat
      )
      // sort within a group
      .map(
        x => (x._1, x._2.sortBy(_._2))
      )
      .flatMap(_._2)
      .map(_._3)
  }

  def mkString(p: AddressLookup) =
    List[String](p.addressLine1.getOrElse(""), p.addressLine2.getOrElse(""), p.addressLine3.getOrElse(""), p.addressLine4.getOrElse(""))
      .mkString(" ")
      .toLowerCase()

  // Find numbers in proposed address in order of significance, from rightmost to leftmost.
  // Pad with None to ensure we never return an empty sequence
  def numbersIn(p: AddressLookup): Seq[Option[Int]] =
    "([0-9]+)".r
      .findAllIn(mkString(p))
      .map(
        n => Try(n.toInt).toOption
      )
      .toSeq
      .reverse :+ None

  def sort_v2(items: Seq[AddressLookup]): Seq[AddressLookup] =
    items.sortWith {
      (a, b) =>
        def sort(zipped: Seq[(Option[Int], Option[Int])]): Boolean = zipped match {
          case (Some(nA), Some(nB)) :: tail =>
            if (nA == nB) sort(tail) else nA < nB
          case (Some(_), None) :: _ => true
          case (None, Some(_)) :: _ => false
          case _                    => mkString(a) < mkString(b)
        }

        sort(numbersIn(a).zipAll(numbersIn(b), None, None).toList)
    }
}
