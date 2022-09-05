package api.trace.common

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.IDCard
import services.TraceService.removeTrace

case class UserDeleteTraceMessage(userToken: String, idCard: IDCard, time: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, removeTrace(userToken, idCard, time, now).get)
  }
}
