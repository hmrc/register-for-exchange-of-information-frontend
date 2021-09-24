package connectors

import config.FrontendAppConfig
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MatchingConnector @Inject() (http: HttpClient, config: FrontendAppConfig) {
  private val logger: Logger = Logger(this.getClass)

  def isBusinessMatching()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    ???
  }

  def isIndividualMatching()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    ???
  }

}
