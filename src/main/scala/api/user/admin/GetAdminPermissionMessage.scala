package api.user.admin

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.MicroServiceToken
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetAdminPermission(secret: MicroServiceToken, userToken: String, `type`: String = "GetAdminPermission") extends JacksonSerializable

case class GetAdminPermissionMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetAdminPermission(MicroServiceTokens.impl.user, userToken).send[TSMSPReply](MicroServicePorts.user.APIUrl).get
  }
}
