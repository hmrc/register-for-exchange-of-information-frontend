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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  val contactHost: String          = configuration.get[String]("contact-frontend.host")
  val contactFormServiceIdentifier = "MDR"

  lazy val addressLookUpUrl: String = configuration.get[Service]("microservice.services.address-lookup").baseUrl

  val taxEnrolmentsUrl1: String =
    s"${configuration.get[Service]("microservice.services.tax-enrolments").baseUrl}${configuration
        .get[String]("microservice.services.tax-enrolments.url1")}"
  val taxEnrolmentsUrl2: String = s"${configuration.get[String]("microservice.services.tax-enrolments.url2")}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  def loginContinueUrl: String = s"${configuration.get[String]("urls.loginContinue")}"
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  val timeoutSeconds: Int   = configuration.get[Int]("session.timeoutSeconds")
  val countdownSeconds: Int = configuration.get[Int]("session.countdownSeconds")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val businessMatchingUrl: String =
    s"${configuration.get[Service]("microservice.services.business-matching").baseUrl}${configuration.get[String]("microservice.services.business-matching.startUrl")}"

  lazy val enrolmentStoreProxyUrl: String =
    s"${configuration.get[Service]("microservice.services.enrolment-store-proxy").baseUrl}${configuration.get[String]("microservice.services.enrolment-store-proxy.startUrl")}"

  lazy val sendEmailUrl: String              = configuration.get[Service]("microservice.services.email").baseUrl
  lazy val emailOrganisationTemplate: String = configuration.get[String]("emailtemplates.organisation")

  lazy val emailIndividualTemplate: String = configuration.get[String]("emailtemplates.individual")
  lazy val emailSoleTraderTemplate: String = configuration.get[String]("emailtemplates.individual")
  lazy val lostUTRUrl: String              = configuration.get[String]("urls.lostUTR")

  lazy val businessTaxAccountLink: String              = configuration.get[String]("urls.businessTaxAccount")
  lazy val findAndUpdateCompanyInfoLink: String        = configuration.get[String]("urls.findAndUpdateCompanyInfo")
  lazy val emailEnquiries: String                      = configuration.get[String]("urls.emailEnquiries")
  lazy val countryCodeJson: String                     = configuration.get[String]("json.countries")
  val enrolmentKey: String                             = configuration.get[String](s"keys.enrolmentKey.mdr")
  val ctEnrolmentKey: String                           = configuration.get[String]("keys.enrolmentKey.ct")
  lazy val mandatoryDisclosureRulesFrontendUrl: String =
    configuration.get[String]("urls.mandatory-disclosure-rules-frontend")
}
