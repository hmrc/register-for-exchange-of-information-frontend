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

package base

import models.matching.SafeId
import models.{UniqueTaxpayerReference, UserAnswers}
import org.mockito.MockitoSugar
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  val userAnswersId = "id"

  def emptyUserAnswers = UserAnswers(userAnswersId, Json.obj())

  implicit val hc: HeaderCarrier   = HeaderCarrier()
  val utr: UniqueTaxpayerReference = UniqueTaxpayerReference("UTR")
  val safeId: SafeId               = SafeId("SAFEID")

}
