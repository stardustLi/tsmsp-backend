package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import models.Trace
import services.TraceService.updateTrace

case class UserUpdateTraceMessage(userToken: String, idCard: IDCard, time: Long, trace: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, updateTrace(userToken, idCard, time, trace, now).get)
  }
}
