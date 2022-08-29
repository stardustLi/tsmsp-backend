package models.api

import org.joda.time.DateTime
import scala.util.Try

import models.{HandleStatus, TSMSPReply, Trace}
import service.TraceService.updateTrace

case class UserUpdateTraceMessage(userToken: String, time: Long, trace: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, updateTrace(userToken, time, trace, now).get)
    /*
    val userName = UserTokenTable.checkUserName(userToken, now).get

    DBUtils.exec(UserTraceTable.updateTrace(userName, trace, time).get)
    TSMSPReply(HandleStatus.OK, "更新成功！")*/
  }
}
