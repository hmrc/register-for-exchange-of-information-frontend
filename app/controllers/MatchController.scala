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
import pages.{BusinessNamePage, BusinessTypePage, RegistrationInfoPage, SoleNamePage, UTRPage}
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
                                  navigator: MDRNavigator,
                                  val controllerComponents: MessagesControllerComponents,
                                  matchingService: BusinessMatchingService,
                                  renderer: Renderer
                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport with WithEitherT {

  def onBusinessMatch(mode: Mode, regime: Regime): Action[AnyContent] = (identify(regime) andThen getData.apply andThen requireData(regime)).async {
    implicit request =>
      (for {
        utr              <- getEither(UTRPage)
        businessName     <- getEither(BusinessNamePage).orElse(getEither(SoleNamePage).map(_.fullName))
        businessType     <- getEither(BusinessTypePage)
        registrationInfo <- EitherT(matchingService.sendBusinessRegistrationInformation(regime, utr, businessName, businessType))
        updatedAnswers   <- setEither(RegistrationInfoPage, registrationInfo)
        _ = sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(RegistrationInfoPage, mode, regime, updatedAnswers))
      ).valueOrF {
        case _ =>
          renderer.render("thereIsAProblem.njk").map(InternalServerError(_))
      }

  }

}
