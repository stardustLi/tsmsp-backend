package api.user.permission

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, UserName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class RevokePermission(secret: MicroServiceToken, userToken: String, other: UserName, `type`: String = "RevokePermission") extends JacksonSerializable

case class UserRevokePermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    RevokePermission(MicroServiceTokens.impl.user, userToken, other).send[TSMSPReply](MicroServicePorts.user.APIUrl).get
  }
}
