package Impl.Messages

import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTokenTable, UserTraceTable}
import Utils.DBUtils
import org.joda.time.DateTime

import scala.util.Try

case class UserUpdateTraceMessage(userToken: String, time: Long, trace: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    val userName = UserTokenTable.checkUserName(userToken, now).get

    DBUtils.exec(UserTraceTable.updateTrace(userName, trace, time).get)
    TSMSPReply(STATUS_OK, "更新成功！")
  }
}
