package models.api

import models.{HandleStatus, TSMSPReply, Trace}
import org.joda.time.DateTime
import service.PolicyService.policyUpdate

import scala.util.Try

case class PolicyUpdateMessage(place: Trace, content: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, policyUpdate(place, content).get)
  }
}
