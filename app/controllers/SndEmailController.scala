package controllers

import controllers.actions._
import forms.SndEmailFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.SndEmailPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class SndEmailController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SndEmailFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"       -> form,
      "action"     -> routes.SndEmailController.onSubmit(mode).url
    )
    renderer.render("sndEmail.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      render(mode, request.userAnswers.get(SndEmailPage).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SndEmailPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SndEmailPage, mode, request.userAnswers))
      )
  }
}
