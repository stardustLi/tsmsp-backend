package models.api.trace.common

import models.Trace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.addTrace

import scala.util.Try

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
  }
}
