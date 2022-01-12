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

package utils

import base.SpecBase
import config.FrontendAppConfig
import models.Country
import org.mockito.ArgumentMatchers.any
import play.api.Environment
import play.api.libs.json.Json

import java.io.ByteArrayInputStream

class CountryListFactorySpec extends SpecBase {
  "Factory  must " - {
    "return option of country sequence when given a valid json file" in {

      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      val countries = Json.arr(Json.obj("state" -> "valid", "code" -> "XX", "description" -> "Somewhere"))

      when(conf.countryCodeJson).thenReturn("countries.json")

      val is = new ByteArrayInputStream(countries.toString.getBytes)
      when(env.resourceAsStream(any())).thenReturn(Some(is))

      val factory = sut(env, conf)

      factory.countryList mustBe Some(Seq(Country("valid", "XX", "Somewhere")))
    }

    "return None when country list cannot be loaded from environment" in {
      val conf: FrontendAppConfig = mock[FrontendAppConfig]
      val env                     = mock[Environment]

      when(conf.countryCodeJson).thenReturn("doesntmatter.json")
      when(env.resourceAsStream(any())).thenReturn(None)

      val factory = sut(env, conf)

      factory.countryList mustBe None
    }

  }

  def sut(env: Environment = mock[Environment], config: FrontendAppConfig = mock[FrontendAppConfig]) =
    new CountryListFactory(env, config)
}
