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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.viewmodels._

sealed trait WhatAreYouRegisteringAs

object WhatAreYouRegisteringAs extends Enumerable.Implicits {

  case object RegistrationTypeBusiness extends WithName("registrationTypeBusiness") with WhatAreYouRegisteringAs
  case object RegistrationTypeIndividual extends WithName("registrationTypeIndividual") with WhatAreYouRegisteringAs

  val values: Seq[WhatAreYouRegisteringAs] = Seq(
    RegistrationTypeBusiness,
    RegistrationTypeIndividual
  )

  def radios(form: Form[_]): Seq[Radios.Item] = {

    val field = form("value")
    val items = Seq(
      Radios.Radio(msg"whatAreYouRegisteringAs.registrationTypeBusiness", RegistrationTypeBusiness.toString),
      Radios.Radio(msg"whatAreYouRegisteringAs.registrationTypeIndividual", RegistrationTypeIndividual.toString)
    )

    Radios(field, items)
  }

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whatAreYouRegisteringAs.${value.toString}")),
        value = Some(value.toString),
        id = if (index == 0) Some(s"value") else Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhatAreYouRegisteringAs] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
