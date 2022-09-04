package models.api.trace.common

import org.joda.time.DateTime

import scala.util.Try
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, TraceID}
import services.TraceService.addTrace

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
  }
}
