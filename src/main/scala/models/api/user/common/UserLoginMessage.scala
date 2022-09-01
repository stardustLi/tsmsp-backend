package models.api.user.common

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.{Password, UserName}
import org.joda.time.DateTime
import services.UserService.login

import scala.util.Try

case class UserLoginMessage(userName: UserName, password: Password) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, login(userName, password, now).get)
  }
}
