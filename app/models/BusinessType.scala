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

package models

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels._

sealed trait BusinessType

object BusinessType extends Enumerable.Implicits {

  case object Partnership extends WithName("partnerShip") with BusinessType
  case object LimitedLiability extends WithName("limitedLiability") with BusinessType
  case object CorporateBody extends WithName("corporateBody") with BusinessType
  case object UnIncorporatedBody extends WithName("unIncorporatedBody") with BusinessType
  case object NotSpecified extends WithName("notSpecified") with BusinessType

  val values: Seq[BusinessType] = Seq(
    CorporateBody,
    NotSpecified,
    Partnership,
    LimitedLiability,
    UnIncorporatedBody
  )

  def radios(form: Form[_])(implicit messages: Messages): Seq[Radios.Item] = {

    val field = form("value")
    val items = Seq(
      Radios.Radio(msg"businessType.corporateBody", CorporateBody.toString),
      Radios.Radio(msg"businessType.notSpecified", NotSpecified.toString),
      Radios.Radio(msg"businessType.partnerShip", Partnership.toString),
      Radios.Radio(msg"businessType.limitedLiability", LimitedLiability.toString),
      Radios.Radio(msg"businessType.unIncorporatedBody", UnIncorporatedBody.toString)
    )

    Radios(field, items)
  }

  implicit val enumerable: Enumerable[BusinessType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
