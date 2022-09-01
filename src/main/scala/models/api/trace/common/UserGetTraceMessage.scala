package models.api.trace.common

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import services.TraceService.getTraces

case class UserGetTraceMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getTraces(userToken, idCard, startTime, endTime, now).get)
  }
}
