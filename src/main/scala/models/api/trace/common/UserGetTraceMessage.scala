package models.api.trace.common

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.IDCard
import org.joda.time.DateTime
import services.TraceService.getTraces

import scala.util.Try

case class UserGetTraceMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, getTraces(userToken, idCard, startTime, endTime, now).get)
    // case class ResultEntry(trace: String, time: Long)

    // val userName = UserTokenTable.checkUserName(userToken, now).get

    // val result = UserTraceTable.checkTrace(userName, startTime, endTime)
    //   .get
    //   .map(trace => new ResultEntry(trace.trace, trace.time))
    // TSMSPReply(HandleStatus.OK, result)
  }
}
