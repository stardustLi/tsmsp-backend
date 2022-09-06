package api.user.common

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.MicroServiceToken
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetProfile(secret: MicroServiceToken, userToken: String, `type`: String = "GetProfile") extends JacksonSerializable

case class UserGetProfileMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetProfile(MicroServiceTokens.impl.user, userToken).send(MicroServicePorts.user.APIUrl).get
  }
}
