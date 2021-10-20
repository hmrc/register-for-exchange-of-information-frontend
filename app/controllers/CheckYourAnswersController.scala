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

package controllers

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.BusinessType.Sole
import models.{Mode, UserAnswers, WhatAreYouRegisteringAs}
import models.WhatAreYouRegisteringAs.{RegistrationTypeBusiness, RegistrationTypeIndividual}
import models.requests.DataRequest
import navigation.Navigator
import pages.{BusinessTypePage, DoYouHaveUniqueTaxPayerReferencePage, WhatAreYouRegisteringAsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{CheckYourAnswersHelper, CountryListFactory}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  countryFactory: CountryListFactory,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val helper                                = new CheckYourAnswersHelper(request.userAnswers, countryListFactory = countryFactory)
      val businessDetails: Seq[SummaryList.Row] = helper.buildDetails(helper)

      val contactHeading = if (getRegisteringAsBusiness()) "checkYourAnswers.firstContact.h2" else "checkYourAnswers.contactDetails.h2"

      val header: String =
        (request.userAnswers.get(BusinessTypePage), request.userAnswers.get(WhatAreYouRegisteringAsPage)) match {
          case (Some(_), _)                                                => "checkYourAnswers.businessDetails.h2"
          case (_, Some(WhatAreYouRegisteringAs.RegistrationTypeBusiness)) => "checkYourAnswers.businessDetails.h2"
          case _                                                           => "checkYourAnswers.individualDetails.h2"
        }

      renderer
        .render(
          "checkYourAnswers.njk",
          Json.obj(
            "header"              -> header,
            "contactHeading"      -> contactHeading,
            "isBusiness"          -> getRegisteringAsBusiness(),
            "businessDetailsList" -> businessDetails,
            "firstContactList"    -> helper.buildFirstContact,
            "secondContactList"   -> helper.buildSecondContact,
            "action"              -> Navigator.checkYourAnswers.url // todo change once backend for onSubmit is implemented
          )
        )
        .map(Ok(_))
  }

  private def getRegisteringAsBusiness()(implicit request: DataRequest[AnyContent]): Boolean =
    (request.userAnswers.get(WhatAreYouRegisteringAsPage),
     request.userAnswers.get(DoYouHaveUniqueTaxPayerReferencePage),
     request.userAnswers.get(BusinessTypePage)
    ) match { //ToDo defaulting to registering for business change when paths created if necessary
      case (None, Some(true), Some(Sole))                   => false
      case (None, Some(true), _)                            => true
      case (Some(RegistrationTypeBusiness), Some(false), _) => true
      case _                                                => false
    }
}
