package api.user.admin

import scala.util.Try

import api.TSMSPMessage
import models.UserAdminPermission
import models.fields.MicroServiceToken
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class SetAdminPermission(secret: MicroServiceToken, userToken: String, permission: UserAdminPermission) extends ExoticMessage

case class SetAdminPermissionMessage(userToken: String, permission: UserAdminPermission) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    SetAdminPermission(MicroServiceTokens.impl.user, userToken, permission)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
