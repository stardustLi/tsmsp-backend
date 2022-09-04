package models.api.user.permission

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.UserName
import services.UserService.revokePermission

case class UserRevokePermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, revokePermission(userToken, other, now).get)
  }
}
