package models.api.user.admin

import scala.util.Try

import models.UserAdminPermission
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.UserService.apiSetAdminPermission

case class SetAdminPermissionMessage(userToken: String, permission: UserAdminPermission) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiSetAdminPermission(userToken, permission, now).get)
  }
}
