package models.api

import org.joda.time.DateTime
import scala.util.Try

import models.fields.UserName
import models.{HandleStatus, TSMSPReply}
import service.UserService.login

case class UserLoginMessage(userName: UserName, password: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, login(userName, password, now))
  }
}
