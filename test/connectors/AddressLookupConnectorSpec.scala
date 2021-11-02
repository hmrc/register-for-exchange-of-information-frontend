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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.AddressLookup
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.address-lookup.port" -> server.port()
    )
    .build()

  lazy val connector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]
  val addressLookupUrl                       = "/lookup"
  val postcode: String                       = "ZZ1 1ZZ"

  def addressJson: String =
    s"""[{
       |  "id": "GB200000698110",
       |  "uprn": 200000706253,
       |  "address": {
       |     "lines": [
       |         "1 Address line 1 Road",
       |         "Address line 2 Road"
       |     ],
       |     "town": "Town",
       |     "county": "County",
       |     "postcode": "$postcode",
       |     "subdivision": {
       |         "code": "GB-ENG",
       |         "name": "England"
       |     },
       |     "country": {
       |         "code": "UK",
       |         "name": "United Kingdom"
       |     }
       |  },
       |  "localCustodian": {
       |      "code": 1760,
       |      "name": "Test Valley"
       |  },
       |  "location": [
       |      50.9986451,
       |      -1.4690977
       |  ],
       |  "language": "en"
       |}]""".stripMargin

  "connectors.AddressLookupConnector" - {
    "when calling addressLookupByPostcode" - {
      "must return 200 (OK) status and an empty list if no match found" in {
        stubResponse(addressLookupUrl, OK, "[]")

        val result = connector.addressLookupByPostcode(postcode)
        result.futureValue mustBe Nil
      }

      "must return 200 (OK) status for submission of valid postcode. Second line is set as address line 3" in {

        stubResponse(addressLookupUrl, OK, addressJson)

        val addressLookupResult = Seq(
          AddressLookup(Some("1 Address line 1 Road"), None, Some("Address line 2 Road"), None, "Town", Some("County"), postcode)
        )

        val result = connector.addressLookupByPostcode(postcode)
        result.futureValue mustBe addressLookupResult
      }

      "must throw an exception when address lookup returns a 400 (BAD_REQUEST) status" in {
        stubResponse(addressLookupUrl, BAD_REQUEST, "Some error")

        val result = connector.addressLookupByPostcode(postcode)

        assertThrows[Exception] {
          result.futureValue
        }
      }

      "must throw an exception when address lookup returns a 404 (NOT_FOUND) status" in {
        stubResponse(addressLookupUrl, NOT_FOUND, "Some error")

        val result = connector.addressLookupByPostcode(postcode)

        assertThrows[Exception] {
          result.futureValue
        }
      }

      "must throw an exception when address lookup returns a 405 (METHOD_NOT_ALLOWED) status" in {
        stubResponse(addressLookupUrl, METHOD_NOT_ALLOWED, "Some error")

        val result = connector.addressLookupByPostcode(postcode)

        assertThrows[Exception] {
          result.futureValue
        }
      }

      "must throw an exception when address lookup returns a 500 (INTERNAL_SERVER_ERROR) status" in {
        stubResponse(addressLookupUrl, INTERNAL_SERVER_ERROR, "Some error")

        val result = connector.addressLookupByPostcode(postcode)

        assertThrows[Exception] {
          result.futureValue
        }
      }

      def addressesJson: String =
        s"""[
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Flat 3",
           |         "7 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Flat 2",
           |         "7 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "5 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "4 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "3 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "Flat 1",
           |         "7 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "2 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |},
           |{
           |  "id": "GB200000698110",
           |  "uprn": 200000706253,
           |  "address": {
           |     "lines": [
           |         "8 Other place"
           |     ],
           |     "town": "Town",
           |     "county": "County",
           |     "postcode": "$postcode",
           |     "subdivision": {
           |         "code": "GB-ENG",
           |         "name": "England"
           |     },
           |     "country": {
           |         "code": "UK",
           |         "name": "United Kingdom"
           |     }
           |  },
           |  "localCustodian": {
           |      "code": 1760,
           |      "name": "Test Valley"
           |  },
           |  "location": [
           |      50.9986451,
           |      -1.4690977
           |  ],
           |  "language": "en"
           |}]""".stripMargin

      "must return sorted addresses" in {
        stubResponse(addressLookupUrl, OK, addressesJson)

        val addressLookupResult = Vector(
          AddressLookup(Some("2 Other place"), None, None, None, "Town", Some("County"), postcode),
          AddressLookup(Some("3 Other place"), None, None, None, "Town", Some("County"), postcode),
          AddressLookup(Some("4 Other place"), None, None, None, "Town", Some("County"), postcode),
          AddressLookup(Some("5 Other place"), None, None, None, "Town", Some("County"), postcode),
          AddressLookup(Some("Flat 1"), None, Some("7 Other place"), None, "Town", Some("County"), postcode),
          AddressLookup(Some("Flat 2"), None, Some("7 Other place"), None, "Town", Some("County"), postcode),
          AddressLookup(Some("Flat 3"), None, Some("7 Other place"), None, "Town", Some("County"), postcode),
          AddressLookup(Some("8 Other place"), None, None, None, "Town", Some("County"), postcode)
        )

        val result = connector.addressLookupByPostcode(postcode)

        result.futureValue mustBe addressLookupResult
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )
}
