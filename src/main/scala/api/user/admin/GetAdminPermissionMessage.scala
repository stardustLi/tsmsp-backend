package api.user.admin

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import scala.util.Try
import services.UserService.apiGetAdminPermission

case class GetAdminPermissionMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, apiGetAdminPermission(userToken, now).get)
  }
}
