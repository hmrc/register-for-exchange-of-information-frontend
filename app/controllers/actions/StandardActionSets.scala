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

package controllers.actions

import models.Regime
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.{ActionBuilder, AnyContent}
import queries.Gettable

import javax.inject.Inject

class StandardActionSets @Inject() (identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    initializeData: DataInitializeAction,
                                    dependantAnswer: DependantAnswerProvider
) {

  def identifiedUserWithInitializedData(regime: Regime): ActionBuilder[DataRequest, AnyContent] =
    identify(regime) andThen getData() andThen initializeData(regime)

  def identifiedUserWithData(regime: Regime): ActionBuilder[DataRequest, AnyContent] =
    identify(regime) andThen getData() andThen requireData(regime)

  def identifiedUserWithDependantAnswer[T](answer: Gettable[T], regime: Regime)(implicit reads: Reads[T]): ActionBuilder[DataRequest, AnyContent] =
    identifiedUserWithData(regime) andThen dependantAnswer(answer, regime)

}
