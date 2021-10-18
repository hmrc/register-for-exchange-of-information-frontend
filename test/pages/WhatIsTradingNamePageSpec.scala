package pages

import models.WhatIsTradingName
import pages.behaviours.PageBehaviours

class WhatIsTradingNamePageSpec extends PageBehaviours {

  "WhatIsTradingNamePage" - {

    beRetrievable[WhatIsTradingName](WhatIsTradingNamePage)

    beSettable[WhatIsTradingName](WhatIsTradingNamePage)

    beRemovable[WhatIsTradingName](WhatIsTradingNamePage)
  }
}
