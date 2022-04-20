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

package models

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.PathBindable

class RegimeSpec extends AnyFreeSpec with Matchers with EitherValues {

  "Regime" - {
    val pathBindable = implicitly[PathBindable[Regime]]

    "bind to `mdr` from path" in {
      val result =
        pathBindable.bind("key", "mdr").value

      result mustEqual MDR
    }

    "fail to bind anything else from path" in {

      val result =
        pathBindable.bind("key", "foobar").left.value

      result mustEqual "Unknown Regime"
    }

    "unbind from `mdr` to path" in {

      val result =
        pathBindable.unbind("key", MDR)

      result mustEqual "mdr"
    }

    "bind to `cbc` from path" in {
      val result =
        pathBindable.bind("key", "mdr").value

      result mustEqual MDR
    }

    "unbind from `cbc` to path" in {

      val result =
        pathBindable.unbind("key", MDR)

      result mustEqual "mdr"
    }
  }
}
