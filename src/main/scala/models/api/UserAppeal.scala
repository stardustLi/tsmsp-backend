package models.api

import org.joda.time.DateTime
import scala.util.Try

import models.{HandleStatus, TSMSPReply}
import service.TraceService.getTraces

case class UserAppealMessage(realName: String, idCard: String, reason: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addAppeal(realName, idCard, reason, now).get)
    // case class ResultEntry(trace: String, time: Long)

    // val userName = UserTokenTable.checkUserName(userToken, now).get

    // val result = UserTraceTable.checkTrace(userName, startTime, endTime)
    //   .get
    //   .map(trace => new ResultEntry(trace.trace, trace.time))
    // TSMSPReply(HandleStatus.OK, result)
  }
}

