package pages

import models.ReporterType
import pages.behaviours.PageBehaviours

class ReporterTypePageSpec extends PageBehaviours {

  "ReporterTypePage" - {

    beRetrievable[Set[ReporterType]](ReporterTypePage)

    beSettable[Set[ReporterType]](ReporterTypePage)

    beRemovable[Set[ReporterType]](ReporterTypePage)
  }
}
