package api.user.admin

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import scala.util.Try

import models.UserAdminPermission
import services.UserService.apiSetAdminPermission

case class SetAdminPermissionMessage(userToken: String, permission: UserAdminPermission) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiSetAdminPermission(userToken, permission, now).get)
  }
}
