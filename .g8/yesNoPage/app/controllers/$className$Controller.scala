package controllers

class $className;format="cap"$Controller @Inject()(
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

  private def render(mode: Mode, form: Form[Boolean])(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val data = Json.obj(
      "form"       -> form,
      "action"     -> routes.$className$Controller.onSubmit(mode).url,
      "radios"     -> Radios.yesNo(form("value"))
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
