package Impl.Messages
import Impl.{STATUS_OK, TSMSPReply}
import Tables.{UserTokenTable, UserTraceTable}
import Utils.IOUtils
import org.joda.time.DateTime

import scala.util.Try

case class UserGetTraceMessage(userToken : String, startTime : Long, endTime : Long) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    val userName = UserTokenTable.checkUserName(userToken).get
    TSMSPReply(STATUS_OK, IOUtils.serialize(UserTraceTable.checkTrace(userName, startTime, endTime).get.map(_.trace)).get)
  }
}
