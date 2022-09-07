package api.user.common

import scala.util.Try

import api.TSMSPMessage
import models.fields.MicroServiceToken
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetProfile(secret: MicroServiceToken, userToken: String) extends ExoticMessage

case class UserGetProfileMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetProfile(MicroServiceTokens.impl.user, userToken)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
