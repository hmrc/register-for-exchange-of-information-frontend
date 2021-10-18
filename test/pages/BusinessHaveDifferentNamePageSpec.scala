package pages

import pages.behaviours.PageBehaviours

class BusinessHaveDifferentNamePageSpec extends PageBehaviours {

  "BusinessHaveDifferentNamePage" - {

    beRetrievable[Boolean](BusinessHaveDifferentNamePage)

    beSettable[Boolean](BusinessHaveDifferentNamePage)

    beRemovable[Boolean](BusinessHaveDifferentNamePage)
  }
}
