package api.user.common

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.{IDCard, Password, UserName}
import services.UserService.register

case class UserRegisterMessage(userName: UserName, password: Password, realName: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, register(userName, password, realName, idCard, now).get)
  }
}