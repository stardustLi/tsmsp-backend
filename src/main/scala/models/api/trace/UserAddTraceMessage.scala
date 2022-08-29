package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.TSMSPMessage
import models.fields.IDCard
import models.{HandleStatus, TSMSPReply, Trace}
import service.TraceService.addTrace

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
  }
}
