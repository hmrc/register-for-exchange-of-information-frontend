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

import play.api.mvc.PathBindable

sealed trait Regime

case object MDR extends WithName("mdr") with Regime

object Regime {
  case class UnknownRegimeException() extends Exception

  implicit def regimePathBindable(implicit stringBinder: PathBindable[String]): PathBindable[Regime] = new PathBindable[Regime] {

    val regimes = Seq(MDR)

    override def bind(key: String, value: String): Either[String, Regime] =
      stringBinder.bind(key, value) match {
        case Right(MDR.toString) => Right(MDR)
        case _                   => Left("Unknown Regime")
      }

    override def unbind(key: String, value: Regime): String = {
      val regimeValue = regimes.find(_ == value).map(_.toString).getOrElse(throw UnknownRegimeException())
      stringBinder.unbind(key, regimeValue)
    }
  }

  def toRegime(string: String): Regime =
    string.toLowerCase match {
      case MDR.toString => MDR
    }
}
