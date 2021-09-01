package pages

import pages.behaviours.PageBehaviours


class ContactNamePageSpec extends PageBehaviours {

  "ContactNamePage" - {

    beRetrievable[String](ContactNamePage)

    beSettable[String](ContactNamePage)

    beRemovable[String](ContactNamePage)
  }
}
