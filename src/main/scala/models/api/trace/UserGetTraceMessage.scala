package models.api.trace

import org.joda.time.DateTime
import scala.util.Try

import models.api.TSMSPMessage
import models.fields.IDCard
import models.{HandleStatus, TSMSPReply}
import service.TraceService.getTraces

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
