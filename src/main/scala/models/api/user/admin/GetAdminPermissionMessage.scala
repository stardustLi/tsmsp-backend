package models.api.user.admin

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.UserService.apiGetAdminPermission

case class GetAdminPermissionMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiGetAdminPermission(userToken, now).get)
  }
}
