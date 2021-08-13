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

package models.requests

import play.api.mvc.{Request, WrappedRequest}
import models.UserAnswers
import play.api.data.Form
import play.api.libs.json.{Reads, Writes}
import queries.{Gettable, Settable}

import scala.concurrent.Future

case class OptionalDataRequest[A](request: Request[A], userId: String, userAnswers: Option[UserAnswers]) extends WrappedRequest[A](request)

case class DataRequest[A](request: Request[A], userId: String, userAnswers: UserAnswers) extends WrappedRequest[A](request) {

  def asForm[B](page: Gettable[B], form: Form[B])(implicit rds: Reads[B]): Form[B] = userAnswers.get(page).fold(form)(form.fill)

  def update[B](page: Settable[B], value: B)(implicit wr: Writes[B]): Future[UserAnswers] = Future.fromTry(userAnswers.set(page, value))
}
