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
import models.error.ApiError.DuplicateSubmissionError
import models.matching.MatchingType.AsIndividual
import models.matching.RegistrationInfo
import models.{CheckMode, Mode, Regime}
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.BusinessMatchingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

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
        nino <- getEither(WhatIsYourNationalInsuranceNumberPage)
        name <- getEither(WhatIsYourNamePage)
        dob = request.userAnswers.get(WhatIsYourDateOfBirthPage)

        _ = println(s"\nnino = $nino name = $name dob = $dob")

        _        <- getEither(RegistrationInfoPage).ensure(DuplicateSubmissionError)(_.existing(AsIndividual, name, nino, dob))
        response <- EitherT(matchingService.sendIndividualRegistrationInformation(regime, RegistrationInfo.build(name, nino, dob)))
        withInfo <- setEither(RegistrationInfoPage, response)
        _ = sessionRepository.set(withInfo)
      } yield Redirect(routes.WeHaveConfirmedYourIdentityController.onPageLoad(regime))).valueOr {
        case DuplicateSubmissionError if mode == CheckMode =>
          Redirect(routes.CheckYourAnswersController.onPageLoad(regime))
        case error =>
          println(s"Individual not matched with error $error") // todo del me
          logger.debug(s"Individual not matched with error $error")
          Redirect(routes.WeCouldNotConfirmController.onPageLoad("identity", regime))
      }
  }

  def onBusinessMatch(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        utr          <- getEither(UTRPage)
        businessName <- getEither(BusinessNamePage).orElse(getEither(SoleNamePage).map(_.fullName))
        businessType <- getEither(BusinessTypePage)
        dateOfBirth = request.userAnswers.get(SoleDateOfBirthPage)
        _        <- getEither(RegistrationInfoPage).ensure(DuplicateSubmissionError)(_.existing(businessType, businessName, utr, dateOfBirth))
        response <- EitherT(matchingService.sendBusinessRegistrationInformation(regime, RegistrationInfo.build(businessType, businessName, utr, dateOfBirth)))
        withInfo <- setEither(RegistrationInfoPage, response)
        _ = sessionRepository.set(withInfo)
      } yield Redirect(routes.IsThisYourBusinessController.onPageLoad(mode, regime))).valueOr {
        case DuplicateSubmissionError if mode == CheckMode =>
          Redirect(routes.CheckYourAnswersController.onPageLoad(regime))
        case error =>
          logger.debug(s"Business not matched with error $error")
          Redirect(routes.NoRecordsMatchedController.onPageLoad(regime))
      }
  }
}
