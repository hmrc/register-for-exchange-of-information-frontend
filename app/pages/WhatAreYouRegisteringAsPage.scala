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

package pages

import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.{UserAnswers, WhatAreYouRegisteringAs}
import pages.PageLists._
import play.api.libs.json.JsPath

import scala.util.Try

case object WhatAreYouRegisteringAsPage extends QuestionPage[WhatAreYouRegisteringAs] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "whatAreYouRegisteringAs"

  override def cleanup(value: Option[WhatAreYouRegisteringAs], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(RegistrationTypeBusiness) =>
        (businessWithIDPages ++ individualWithIDPages ++ individualWithOutIDPages ++ allContactDetailPages).foldLeft(Try(userAnswers))(PageLists.removePage)

      case Some(RegistrationTypeIndividual) =>
        (businessWithIDPages ++ businessWithOutIDPages ++ allContactDetailPages).foldLeft(Try(userAnswers))(PageLists.removePage)
        
    }

}
