package api.user.admin

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.UserAdminPermission
import models.fields.MicroServiceToken
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class SetAdminPermission(secret: MicroServiceToken, userToken: String, permission: UserAdminPermission, `type`: String = "SetAdminPermission") extends JacksonSerializable

case class SetAdminPermissionMessage(userToken: String, permission: UserAdminPermission) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    SetAdminPermission(MicroServiceTokens.impl.user, userToken, permission)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
