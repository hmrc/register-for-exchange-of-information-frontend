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

import cats.data.EitherT
import cats.implicits._
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{Mode, Regime}
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.RegistrationInfo
import models.requests.DataRequest
import navigation.MDRNavigator
import pages.{
  BusinessNamePage,
  BusinessTypePage,
  RegistrationInfoPage,
  SoleNamePage,
  UTRPage,
  WhatIsYourDateOfBirthPage,
  WhatIsYourNamePage,
  WhatIsYourNationalInsuranceNumberPage
}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.BusinessMatchingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MatchController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  matchingService: BusinessMatchingService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with WithEitherT
    with Logging {

  def onIndividualMatch(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        nino             <- getEither(WhatIsYourNationalInsuranceNumberPage)
        name             <- getEither(WhatIsYourNamePage)
        dob              <- getEither(WhatIsYourDateOfBirthPage)
        registrationInfo <- EitherT(matchingService.sendIndividualRegistratonInformation(regime, nino, name, dob))
        updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
        _ = sessionRepository.set(updatedAnswers)
      } yield Redirect(routes.WeHaveConfirmedYourIdentityController.onPageLoad(regime))).valueOr {
        error =>
          logger.debug(s"Individual not matched with error $error")
          Redirect(routes.WeCouldNotConfirmController.onPageLoad("identity", regime))
      }
  }

  def onBusinessMatch(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        utr              <- getEither(UTRPage)
        businessName     <- getEither(BusinessNamePage).orElse(getEither(SoleNamePage).map(_.fullName))
        businessType     <- getEither(BusinessTypePage)
        registrationInfo <- EitherT(matchingService.sendBusinessRegistrationInformation(regime, utr, businessName, businessType))
        updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
        _ = sessionRepository.set(updatedAnswers)
      } yield Redirect(routes.IsThisYourBusinessController.onPageLoad(mode, regime))).valueOr {
        error =>
          logger.debug(s"Business not matched with error $error")
          Redirect(routes.NoRecordsMatchedController.onPageLoad(regime))
      }
  }

}
