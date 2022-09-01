package models.api.trace.common

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.removeTrace

import scala.util.Try

case class UserDeleteTraceMessage(userToken: String, idCard: IDCard, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, removeTrace(userToken, idCard, time, now).get)
  }
}
