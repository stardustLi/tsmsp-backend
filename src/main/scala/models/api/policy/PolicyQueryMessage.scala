package models.api.policy

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import org.joda.time.DateTime
import services.PolicyService.policyQuery

import scala.util.Try

case class PolicyQueryMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyQuery(place).get)
  }
}
