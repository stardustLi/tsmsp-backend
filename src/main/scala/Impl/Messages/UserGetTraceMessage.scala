package Impl.Messages

import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTokenTable, UserTraceTable}
import Utils.IOUtils
import org.joda.time.DateTime

import scala.util.Try

case class UserGetTraceMessage(userToken: String, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    case class ResultEntry(trace: String, time: Long)

    val userName = UserTokenTable.checkUserName(userToken, now).get

    val result = UserTraceTable.checkTrace(userName, startTime, endTime)
      .get
      .map(trace => new ResultEntry(trace.trace, trace.time))
    TSMSPReply(STATUS_OK, result)
  }
}
