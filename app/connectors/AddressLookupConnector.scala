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
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

/*

todo del me

todo response SORTOWAC -> adrLine1 !!!!
response= List(AddressLookup(Some(10 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(2 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(3 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(4 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(5 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(6 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(8 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(9 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 1),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 2),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 3),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ))

addresses= List(AddressLookup(Some(10 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(2 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(3 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(4 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(5 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(6 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(8 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(9 Other Place),None,Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 1),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 2),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ), AddressLookup(Some(Flat 3),Some(7 Other Place),Some(Some District),None,Anytown,None,ZZ1 1ZZ))

adrsItems= List(Radio(Message(10 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),10 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(2 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),2 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(3 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),3 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(4 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),4 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(5 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),5 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(6 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),6 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(8 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),8 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(9 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),9 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(Flat 1, 7 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),Flat 1, 7 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(Flat 2, 7 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),Flat 2, 7 Other Place, Some District, Anytown, ZZ1 1ZZ), Radio(Message(Flat 3, 7 Other Place, Some District, Anytown, ZZ1 1ZZ,WrappedArray()),Flat 3, 7 Other Place, Some District, Anytown, ZZ1 1ZZ))

 */
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

        Future.successful(doubleSort(adrBase))
      case response =>
        val message = s"Address Lookup failed with status ${response.status} Response body: ${response.body}"
        Future.failed(new HttpException(message, response.status))
    } recover {
      case e: Exception =>
        logger.error("Exception in Address Lookup", e)
        throw e
    }
  }

  private def doubleSort(items: Seq[AddressLookup]): Seq[AddressLookup] =
    items
      .map(
        item =>
          (item.addressLine1.map(
             a => ("""^[a-zA-Z]*""".r findAllIn a).toList.mkString
           ), // chars from address_1
           item.addressLine1.map(
             b => ("""\d+""".r findAllIn b).toList.mkString.toInt
           ), // int from address_1
           item // whole address
          )
      )
      .groupBy(_._1)
      .flatMap(
        x => x._2.sortBy(_._2)
      )
      .map(_._3)
      .toSeq
}
