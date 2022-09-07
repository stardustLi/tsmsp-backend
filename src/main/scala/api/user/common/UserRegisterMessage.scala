package api.user.common

import scala.util.Try

import api.TSMSPMessage
import models.fields.{IDCard, MicroServiceToken, Password, UserName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Register(secret: MicroServiceToken, userName: UserName, password: Password, realName: String, idCard: IDCard) extends ExoticMessage

case class UserRegisterMessage(userName: UserName, password: Password, realName: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    Register(MicroServiceTokens.impl.user, userName, password, realName, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
