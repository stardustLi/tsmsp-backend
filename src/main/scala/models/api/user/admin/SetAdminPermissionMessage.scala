package models.api.user.admin

import models.UserAdminPermission
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.UserService.apiSetPermission

import scala.util.Try

case class SetAdminPermissionMessage(userToken: String, permission: UserAdminPermission) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiSetPermission(userToken, permission, now).get)
  }
}
