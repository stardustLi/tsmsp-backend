package models.api

import models.{HandleStatus, TSMSPReply, Trace, UserPermission}
import org.joda.time.DateTime
import service.UserService.apiSetPermission

import scala.util.Try

case class SetPermissionMessage(userToken: String, permission: UserPermission) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiSetPermission(userToken, permission, now).get)
  }
}
