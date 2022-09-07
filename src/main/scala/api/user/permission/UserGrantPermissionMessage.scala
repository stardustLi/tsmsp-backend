package api.user.permission

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, UserName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GrantPermission(secret: MicroServiceToken, userToken: String, other: UserName) extends ExoticMessage

case class UserGrantPermissionMessage(userToken: String, other: UserName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GrantPermission(MicroServiceTokens.impl.user, userToken, other)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
