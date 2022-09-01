package models.api.user.common

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.Password
import services.UserService.changePassword

case class UserChangePasswordMessage(userToken: String, newPassword: Password) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, changePassword(userToken, newPassword, now).get)
  }
}
