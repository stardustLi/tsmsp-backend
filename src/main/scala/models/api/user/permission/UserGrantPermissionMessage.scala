package models.api.user.permission

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.UserName
import services.UserService.grantPermission

case class UserGrantPermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, grantPermission(userToken, other, now).get)
  }
}
