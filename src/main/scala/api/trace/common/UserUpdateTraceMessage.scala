package api.trace.common

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{IDCard, TraceID}
import services.TraceService.updateTrace

case class UserUpdateTraceMessage(userToken: String, idCard: IDCard, time: Long, trace: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, updateTrace(userToken, idCard, time, trace, now).get)
  }
}
