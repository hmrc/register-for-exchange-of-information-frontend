package pages

import pages.behaviours.PageBehaviours

class IsContactTelephonePageSpec extends PageBehaviours {

  "IsContactTelephonePage" - {

    beRetrievable[Boolean](IsContactTelephonePage)

    beSettable[Boolean](IsContactTelephonePage)

    beRemovable[Boolean](IsContactTelephonePage)
  }
}
