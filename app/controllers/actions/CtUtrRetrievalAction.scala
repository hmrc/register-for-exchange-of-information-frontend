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

package controllers.actions

import config.FrontendAppConfig
import models.{IdentifierType, UniqueTaxpayerReference}
import models.requests.IdentifierRequest
import play.api.mvc.ActionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CtUtrRetrievalActionImpl @Inject() (
  val config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends CtUtrRetrievalAction {

  override def apply(): ActionTransformer[IdentifierRequest, IdentifierRequest] =
    new CtUtrRetrievalActionProvider(config)
}

class CtUtrRetrievalActionProvider @Inject() (
  val config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[IdentifierRequest, IdentifierRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[IdentifierRequest[A]] = {
    val ctUtr = request.enrolments
      .find(_.key == config.ctEnrolmentKey)
      .flatMap(_.identifiers.collectFirst {
        case i if i.key == IdentifierType.UTR => UniqueTaxpayerReference(i.value)
      })

    Future.successful(request.copy(utr = ctUtr))
  }
}

trait CtUtrRetrievalAction {
  def apply(): ActionTransformer[IdentifierRequest, IdentifierRequest]
}
