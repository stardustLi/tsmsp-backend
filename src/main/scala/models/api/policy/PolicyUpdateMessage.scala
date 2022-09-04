package models.api.policy

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import org.joda.time.DateTime
import services.PolicyService.policyUpdate

import scala.util.Try

case class PolicyUpdateMessage(userToken: String, place: TraceID, content: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyUpdate(userToken, place, content, now).get)
  }
}
