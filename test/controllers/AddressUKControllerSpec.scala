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

package controllers

import base.ControllerSpecBase
import config.FrontendAppConfig
import forms.AddressUKFormProvider
import models.{Address, Country, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.AddressUKPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import utils.CountryListFactory
import views.html.AddressUkView

import scala.concurrent.Future

class AddressUKControllerSpec extends ControllerSpecBase {
  lazy val loadRoute: String   = routes.AddressUKController.onPageLoad(NormalMode).url
  lazy val submitRoute: String = routes.AddressUKController.onSubmit(NormalMode).url

  val ukSelectItem                        = Seq(SelectItem(Some("GB"), "United Kingdom", selected = true))
  val testCountryList: Seq[Country]       = Seq(Country("valid", "GB", "United Kingdom"))
  val formProvider: AddressUKFormProvider = new AddressUKFormProvider()
  val form: Form[Address]                 = formProvider(testCountryList)
  val address: Address                    = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), Country("valid", "GB", "United Kingdom"))

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val countryListFactory: CountryListFactory = new CountryListFactory(app.environment, mockAppConfig) {
    override lazy val countryList: Option[Seq[Country]] = Some(testCountryList)
  }

  val emptyCountryListFactory: CountryListFactory = new CountryListFactory(app.environment, mockAppConfig) {
    override lazy val countryList: Option[Seq[Country]] = None
  }

  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(AddressUKPage, address).success.value

  "AddressUk Controller" - {

    "must return OK and the correct view for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          ukSelectItem,
          NormalMode
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      retrieveUserAnswersData(userAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(address),
          ukSelectItem,
          NormalMode
        ).toString
      }
    }

    "must redirect to ThereIsAProblem page when countriesList can not be retrieved for a GET" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(emptyCountryListFactory)
        )
        .build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, loadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ThereIsAProblemController.onPageLoad().url)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, loadRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"),
                                    ("addressLine2", "value 2"),
                                    ("addressLine3", "value 3"),
                                    ("addressLine4", "value 4"),
                                    ("postCode", "XX9 9XX"),
                                    ("country", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to ThereIsAProblem page when countriesList can not be retrieved when data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(emptyCountryListFactory)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, loadRoute)
          .withFormUrlEncodedBody(("addressLine1", "value 1"),
                                  ("addressLine2", "value 2"),
                                  ("addressLine3", "value 3"),
                                  ("addressLine4", "value 4"),
                                  ("postCode", "XX9 9XX"),
                                  ("country", "GB")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ThereIsAProblemController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      retrieveUserAnswersData(emptyUserAnswers)

      val application = guiceApplicationBuilder()
        .overrides(
          bind[CountryListFactory].to(countryListFactory)
        )
        .build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, loadRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          ukSelectItem,
          NormalMode
        ).toString
      }
    }
  }
}
