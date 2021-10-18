package models.subscription.response

import play.api.libs.json.{Json, Reads}

case class CreateSubscriptionResponseDetail(subscriptionID: String)

object CreateSubscriptionResponseDetail {
  implicit val reads: Reads[CreateSubscriptionResponseDetail] = Json.reads[CreateSubscriptionResponseDetail]
}

case class SubscriptionResponse(responseCommon: ResponseCommon, responseDetail: CreateSubscriptionResponseDetail)

object SubscriptionResponse {
  implicit val reads: Reads[SubscriptionResponse] = Json.reads[SubscriptionResponse]
}
