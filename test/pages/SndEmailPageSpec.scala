package pages

import pages.behaviours.PageBehaviours


class SndEmailPageSpec extends PageBehaviours {

  "SndEmailPage" - {

    beRetrievable[String](SndEmailPage)

    beSettable[String](SndEmailPage)

    beRemovable[String](SndEmailPage)
  }
}
