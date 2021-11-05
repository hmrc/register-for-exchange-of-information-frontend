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

import controllers.actions._
import forms.BusinessNameFormProvider
import models.BusinessType._
import models.requests.DataRequest
import models.{BusinessType, Mode, Regime}
import navigation.MDRNavigator
import pages.{BusinessNamePage, BusinessTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: MDRNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: BusinessNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  // title msg keys
  private val llpTitleKey            = "businessName.title.llp"
  private val partnerTitleKey        = "businessName.title.partnership"
  private val unincorporatedTitleKey = "businessName.title.unincorporated"

  // heading msg keys
  private val llpHeadingKey            = "businessName.heading.llp"
  private val partnerHeadingKey        = "businessName.heading.partnership"
  private val unincorporatedHeadingKey = "businessName.heading.unincorporated"

  // hint msg keys
  private val llpHintKey            = "businessName.hint.llp"
  private val partnerHintKey        = "businessName.hint.partnership"
  private val unincorporatedHintKey = "businessName.hint.unincorporated"

  // required error msg keys
  private val llpReqErrKey            = "businessName.error.required.llp"
  private val partnerReqErrKey        = "businessName.error.required.partner"
  private val unincorporatedReqErrKey = "businessName.error.required.unincorporated"

  // length error msg keys
  private val llpLnErrKey            = "businessName.error.length.llp"
  private val partnerLnErrKey        = "businessName.error.length.partner"
  private val unincorporatedLnErrKey = "businessName.error.length.unincorporated"

  private def pageHeadingAndHint(businessType: BusinessType)(implicit messages: Messages): (String, String, String) =
    businessType match {
      case LimitedPartnership | LimitedCompany => (messages(llpTitleKey), messages(llpHeadingKey), messages(llpHintKey))
      case Partnership                         => (messages(partnerTitleKey), messages(partnerHeadingKey), messages(partnerHintKey))
      case UnincorporatedAssociation           => (messages(unincorporatedTitleKey), messages(unincorporatedHeadingKey), messages(unincorporatedHintKey))
    }

  private def render(mode: Mode, regime: Regime, form: Form[String], businessType: BusinessType)(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val (title, heading, hint) = pageHeadingAndHint(businessType)

    val data = Json.obj(
      "form"     -> form,
      "regime"   -> regime.toUpperCase,
      "titleTxt" -> title,
      "heading"  -> heading,
      "hint"     -> hint,
      "action"   -> routes.BusinessNameController.onSubmit(mode, regime).url
    )
    renderer.render("businessName.njk", data)
  }

  def onPageLoad(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        SomeInformationIsMissing.isMissingInformation(regime, BusinessTypePage) {
          businessType =>
            val form = formProvider(businessType match {
              case LimitedCompany | LimitedPartnership => (llpReqErrKey, llpLnErrKey)
              case Partnership                         => (partnerReqErrKey, partnerLnErrKey)
              case UnincorporatedAssociation           => (unincorporatedReqErrKey, unincorporatedLnErrKey)
            })
            render(mode, regime, request.userAnswers.get(BusinessNamePage).fold(form)(form.fill), businessType).map(Ok(_))
        }
    }

  def onSubmit(mode: Mode, regime: Regime): Action[AnyContent] =
    (identify(regime) andThen getData.apply andThen requireData(regime)).async {
      implicit request =>
        SomeInformationIsMissing.isMissingInformation(regime, BusinessTypePage) {
          businessType =>
            formProvider(businessType match {
              case LimitedCompany | LimitedPartnership => (llpReqErrKey, llpLnErrKey)
              case Partnership                         => (partnerReqErrKey, partnerLnErrKey)
              case UnincorporatedAssociation           => (unincorporatedReqErrKey, unincorporatedLnErrKey)
            })
              .bindFromRequest()
              .fold(
                formWithErrors => render(mode, regime, formWithErrors, businessType).map(BadRequest(_)),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(BusinessNamePage, mode, regime, updatedAnswers))
              )
        }
    }
}
