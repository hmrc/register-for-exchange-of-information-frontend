package forms

import java.time.LocalDate

import forms.mappings.Mappings
import models.DateHelper.today
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "$className;format="decap"$.error.invalid",
        allRequiredKey = "$className;format="decap"$.error.required.all",
        twoRequiredKey = "$className;format="decap"$.error.required.two",
        requiredKey    = "$className;format="decap"$.error.required",
        maxDateKey = "$className;format="decap"$.error.futureDate",
        minDateKey = "$className;format="decap"$.error.pastDate",
        maxDate = today,
        minDate = LocalDate.of(1909, 1, 1)
      )
    )
}
