package models.api.user.permission

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.UserName
import org.joda.time.DateTime
import services.UserService.revokePermission

import scala.util.Try

case class UserRevokePermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, revokePermission(userToken, other, now).get)
  }
}
