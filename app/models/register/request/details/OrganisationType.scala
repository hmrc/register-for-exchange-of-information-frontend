/*
 * Copyright 2023 HM Revenue & Customs
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

package models.register.request.details

import models.BusinessType
import play.api.libs.json._

sealed trait OrganisationType {
  def value: String
}

object OrganisationType {

  implicit val formats: Format[OrganisationType] = new Format[OrganisationType] {

    override def reads(json: JsValue): JsResult[OrganisationType] =
      json.asOpt[String] match {
        case Some("LimitedCompany")            => JsSuccess(LimitedCompany)
        case Some("Sole")                      => JsSuccess(Sole)
        case Some("Partnership")               => JsSuccess(Partnership)
        case Some("LimitedPartnership")        => JsSuccess(LimitedPartnership)
        case Some("UnincorporatedAssociation") => JsSuccess(UnincorporatedAssociation)
        case _                                 => JsError("Invalid OrganisationType value")
      }
    override def writes(businessType: OrganisationType): JsValue = JsString(businessType.value)
  }

  def apply(businessType: BusinessType): OrganisationType = businessType match {
    case BusinessType.LimitedCompany            => LimitedCompany
    case BusinessType.Sole                      => Sole
    case BusinessType.Partnership               => Partnership
    case BusinessType.LimitedPartnership        => LimitedPartnership
    case BusinessType.UnincorporatedAssociation => UnincorporatedAssociation
  }
}

case object LimitedCompany extends OrganisationType {
  def value = "Limited Company"
}

case object Sole extends OrganisationType {
  def value = "Sole"
}

case object Partnership extends OrganisationType {
  def value = "Partnership"
}

case object LimitedPartnership extends OrganisationType {
  def value = "Limited Partnership"
}

case object UnincorporatedAssociation extends OrganisationType {
  def value = "Unincorporated Association"
}
