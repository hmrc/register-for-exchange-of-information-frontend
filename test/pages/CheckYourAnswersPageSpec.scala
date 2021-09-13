package pages

import pages.behaviours.PageBehaviours


class CheckYourAnswersPageSpec extends PageBehaviours {

  "CheckYourAnswersPage" - {

    beRetrievable[String](CheckYourAnswersPage)

    beSettable[String](CheckYourAnswersPage)

    beRemovable[String](CheckYourAnswersPage)
  }
}
