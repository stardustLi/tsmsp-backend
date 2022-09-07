package api.user.common

import scala.util.Try
import api.TSMSPMessage
import models.fields.{MicroServiceToken, Password, UserName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Login(secret: MicroServiceToken, userName: UserName, password: Password) extends ExoticMessage

case class UserLoginMessage(userName: UserName, password: Password) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    Login(MicroServiceTokens.impl.user, userName, password)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
