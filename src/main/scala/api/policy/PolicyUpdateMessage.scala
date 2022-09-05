package api.policy

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.TraceID
import services.PolicyService.policyUpdate

case class PolicyUpdateMessage(userToken: String, place: TraceID, content: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyUpdate(userToken, place, content, now).get)
  }
}
