package models.api.policy

import models.Trace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.PolicyService.policyQuery

import scala.util.Try

case class PolicyQueryMessage(place: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyQuery(place).get)
  }
}
