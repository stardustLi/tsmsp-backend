package api.user.common

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, Password}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class ChangePassword(secret: MicroServiceToken, userToken: String, newPassword: Password) extends ExoticMessage

case class UserChangePasswordMessage(userToken: String, newPassword: Password) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    ChangePassword(MicroServiceTokens.impl.user, userToken, newPassword)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
