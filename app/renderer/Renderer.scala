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

package renderer

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.Regime
import models.error.ApiError
import models.error.ApiError.ServiceUnavailableError
import play.api.Logging
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.mvc.Results.{InternalServerError, ServiceUnavailable}
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.config.TrackingConsentConfig
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Renderer @Inject() (appConfig: FrontendAppConfig, trackingConfig: TrackingConsentConfig, renderer: NunjucksRenderer) extends Logging {

  def render(template: String)(implicit request: RequestHeader): Future[Html] =
    renderTemplate(template, Json.obj())

  def render[A](template: String, ctx: A)(implicit request: RequestHeader, writes: OWrites[A]): Future[Html] =
    renderTemplate(template, Json.toJsObject(ctx))

  def render(template: String, ctx: JsObject)(implicit request: RequestHeader): Future[Html] =
    renderTemplate(template, ctx)

  private def renderTemplate(template: String, ctx: JsObject)(implicit request: RequestHeader): Future[Html] =
    renderer.render(template, ctx ++ Json.obj("config" -> config))

  private lazy val config: JsObject =
    Json.obj(
      "betaFeedbackUnauthenticatedUrl" -> appConfig.betaFeedbackUnauthenticatedUrl,
      "reportAProblemPartialUrl"       -> appConfig.reportAProblemPartialUrl,
      "reportAProblemNonJSUrl"         -> appConfig.reportAProblemNonJSUrl,
      "timeout"                        -> appConfig.timeoutSeconds,
      "countdown"                      -> appConfig.countdownSeconds,
      "trackingConsentScriptUrl"       -> trackingConfig.trackingUrl().get,
      "gtmContainer"                   -> trackingConfig.gtmContainer.get,
      "serviceIdentifier"              -> appConfig.contactFormServiceIdentifier,
      "contactHost"                    -> appConfig.contactHost
    )

  def renderError(error: ApiError, regime: Regime)(implicit request: RequestHeader, ec: ExecutionContext): Future[Result] = {
    val thereIsAProblemView = render(
      "thereIsAProblem.njk",
      Json.obj(
        "regime"       -> regime.toUpperCase,
        "emailAddress" -> appConfig.emailEnquiries
      )
    )
    logger.warn(s"Error received from API: $error")
    error match {
      case ServiceUnavailableError =>
        thereIsAProblemView.map(ServiceUnavailable(_))
      case _ =>
        thereIsAProblemView.map(InternalServerError(_))
    }
  }

  def renderThereIsAProblemPage(regime: Regime)(implicit request: Request[_], ec: ExecutionContext): Future[Result] =
    render("thereIsAProblem.njk", Json.obj("regime" -> regime.toUpperCase, "emailAddress" -> appConfig.emailEnquiries))
      .map(InternalServerError(_))

}
