package Impl.Messages
import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTokenTable, UserTraceTable}
import Utils.DBUtils
import org.joda.time.DateTime

import scala.util.Try

case class UserUpdateTraceMessage(userToken : String, trace : String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    val userName = UserTokenTable.checkUserName(userToken).get
    DBUtils.exec(UserTraceTable.addTrace(userName, trace).get)
    TSMSPReply(STATUS_OK, "上传成功！")
  }
}
