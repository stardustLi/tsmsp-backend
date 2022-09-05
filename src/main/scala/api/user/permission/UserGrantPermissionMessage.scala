package api.user.permission

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.UserName
import services.UserService.grantPermission

case class UserGrantPermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, grantPermission(userToken, other, now).get)
  }
}
