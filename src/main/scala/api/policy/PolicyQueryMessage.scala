package api.policy

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.TraceID
import services.PolicyService.policyQuery

case class PolicyQueryMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyQuery(place).get)
  }
}
