package models.api

import org.joda.time.DateTime

import scala.util.Try
import models.fields.{Password, UserName}
import services.UserService.login

case class UserLoginMessage(userName: UserName, password: Password) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, login(userName, password, now).get)
  }
}
