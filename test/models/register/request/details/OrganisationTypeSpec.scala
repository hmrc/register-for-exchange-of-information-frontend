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

import base.SpecBase
import models.BusinessType
import play.api.libs.json.{JsString, Json}

class OrganisationTypeSpec extends SpecBase {

  "OrganisationType" - {

    "reads" - {

      "must parse valid JSON values to corresponding OrganisationType objects" in {
        Json.fromJson[OrganisationType](JsString("LimitedCompany")).asOpt must contain(LimitedCompany)
        Json.fromJson[OrganisationType](JsString("Sole")).asOpt must contain(Sole)
        Json.fromJson[OrganisationType](JsString("Partnership")).asOpt must contain(Partnership)
        Json.fromJson[OrganisationType](JsString("LimitedPartnership")).asOpt must contain(LimitedPartnership)
        Json.fromJson[OrganisationType](JsString("UnincorporatedAssociation")).asOpt must contain(UnincorporatedAssociation)
      }

      "must return JsError for invalid JSON values" in {
        assert(Json.fromJson[OrganisationType](JsString("InvalidType")).isError)
      }
    }

    "writes" - {

      "must convert OrganisationType objects to JSON values" in {
        Json.toJson[OrganisationType](LimitedCompany) mustBe JsString("Limited Company")
        Json.toJson[OrganisationType](Sole) mustBe JsString("Sole")
        Json.toJson[OrganisationType](Partnership) mustBe JsString("Partnership")
        Json.toJson[OrganisationType](LimitedPartnership) mustBe JsString("Limited Partnership")
        Json.toJson[OrganisationType](UnincorporatedAssociation) mustBe JsString("Unincorporated Association")
      }
    }

    "apply" - {

      "must create OrganisationType from corresponding BusinessType" in {
        OrganisationType(BusinessType.LimitedCompany) mustBe LimitedCompany
        OrganisationType(BusinessType.Sole) mustBe Sole
        OrganisationType(BusinessType.Partnership) mustBe Partnership
        OrganisationType(BusinessType.LimitedPartnership) mustBe LimitedPartnership
        OrganisationType(BusinessType.UnincorporatedAssociation) mustBe UnincorporatedAssociation
      }
    }
  }
}
