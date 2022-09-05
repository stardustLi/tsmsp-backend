package api.trace.common

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{IDCard, TraceID}
import services.TraceService.addTrace

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
  }
}
