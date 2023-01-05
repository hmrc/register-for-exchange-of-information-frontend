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
import play.api.libs.json.{__, Json, OWrites, Reads}

case class Organisation(organisationName: String, organisationType: BusinessType)

object Organisation {

  implicit lazy val writes: OWrites[Organisation] = OWrites[Organisation] {
    organisation =>
      Json.obj(
        "organisationName" -> organisation.organisationName,
        "organisationType" -> OrganisationType(organisation.organisationType).value
      )
  }

  implicit lazy val reads: Reads[Organisation] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "organisationName").read[String] and
        (__ \ "organisationType").read[BusinessType]
    )(
      (organisationName, organisationType) => Organisation(organisationName, organisationType)
    )
  }

}
