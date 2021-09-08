package controllers

import controllers.actions._
import forms.$className$FormProvider
import models.requests.DataRequest

import javax.inject.Inject
import models.{$className$, Mode}
import navigation.Navigator
import pages.$className$Page
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

class $className$Controller @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: $className$FormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[Set[$className$]])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"       -> form,
      "action"     -> routes.$className$Controller.onSubmit(mode).url,
      "checkboxes" -> $className$.checkboxes(form)
    )
    renderer.render("$className;format="decap"$.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      render(mode, request.userAnswers.get($className;format="cap"$Page).fold(form)(form.fill)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
          formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
          value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set($className;format="cap"$Page, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage($className;format="cap"$Page, mode, updatedAnswers))
      )
  }
}
