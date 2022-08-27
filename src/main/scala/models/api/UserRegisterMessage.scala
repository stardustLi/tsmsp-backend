package models.api

import org.joda.time.DateTime
import scala.util.Try

import models.fields.{IDCard, UserName}
import models.{HandleStatus, TSMSPReply}
import service.UserService.register

case class UserRegisterMessage(userName: UserName, password: String, realName: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, register(userName, password, realName, idCard, now))
  }
}
