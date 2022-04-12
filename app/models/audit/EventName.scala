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

package models.audit

import models.{MDR, Regime}

object EventName {
  val CreateSubscription_MDR = "MandatoryDisclosureRulesSubscription"
  val CreateSubscription_CBC = "CountryByCountryReportSubscription"

  def getEventName(regime: Regime): String =
    regime match {
      case MDR => CreateSubscription_MDR
      case _   => CreateSubscription_CBC //TODO may change
    }

}
