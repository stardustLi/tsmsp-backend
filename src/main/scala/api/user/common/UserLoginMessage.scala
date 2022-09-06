package api.user.common

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, Password, UserName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Login(secret: MicroServiceToken, userName: UserName, password: Password, `type`: String = "Login") extends JacksonSerializable

case class UserLoginMessage(userName: UserName, password: Password) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    Login(MicroServiceTokens.impl.user, userName, password)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
