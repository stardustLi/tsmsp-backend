package models.api

import org.joda.time.DateTime
import scala.util.Try

import models.{HandleStatus, TSMSPReply, Trace}
import service.TraceService.addTrace

case class UserAddTraceMessage(userToken: String, trace: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, addTrace(userToken, trace, now))
    // val userName = UserTokenTable.checkUserName(userToken, now).get

    // DBUtils.exec(UserTraceTable.addTrace(userName, trace, now.getMillis()).get)
    // TSMSPReply(HandleStatus.OK, "上传成功！")
  }
}
