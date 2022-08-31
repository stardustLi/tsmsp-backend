package models.api

import org.joda.time.DateTime

import scala.util.Try

case class UserTestMessage(userToken: String, time: Long, trace: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, "更新成功！")
  }
}
