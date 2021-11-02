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

        Future.successful(sortAddresses(adrBase))
      case response =>
        val message = s"Address Lookup failed with status ${response.status} Response body: ${response.body}"
        Future.failed(new HttpException(message, response.status))
    } recover {
      case e: Exception =>
        logger.error("Exception in Address Lookup", e)
        throw e
    }
  }

  private def sortAddresses(items: Seq[AddressLookup]): Seq[AddressLookup] =
    items
      // group
      .map(
        item =>
          (item.addressLine3 match {
            case Some(x) => x
            case None    => (item.addressLine1.getOrElse(""))
          },
            item.addressLine1.map(
              b => ("""\d+""".r findAllIn b).toList.mkString.toInt
            ), // int from address_1
            item // whole address
          )
      ).groupBy(_._1).toSeq
      // sort groups
      .sortBy(
        x => ("""\d+""".r findAllIn x._1).toList.mkString.toInt
      )
      // sort within a group
      .map(
        x => (x._1, x._2.sortBy(_._2))
      )
      .flatMap(_._2)
      .map(_._3)
}
