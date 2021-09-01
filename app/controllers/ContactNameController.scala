package controllers

import controllers.actions._
import forms.ContactNameFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigation.{DefaultJourney, Navigator}
import pages.ContactNamePage
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

class ContactNameController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ContactNameFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  private def render(mode: Mode, form: Form[String])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"       -> form,
      "action"     -> routes.ContactNameController.onSubmit(mode).url
    )
    renderer.render("contactName.njk", data)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      render(mode, request.asForm(pages.ContactNamePage, form)).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => render(mode, formWithErrors).map(BadRequest(_)),
        value =>
          for {
            updatedAnswers <- request.update(ContactNamePage, value)
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ContactNamePage)(DefaultJourney(mode))(Option(value)))
      )
  }
}
