package api.user.permission

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.MicroServiceToken
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class FetchAllGrantedUsers(secret: MicroServiceToken, userToken: String, `type`: String = "FetchAllGrantedUsers") extends JacksonSerializable

case class UserFetchAllGrantedUsersMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    FetchAllGrantedUsers(MicroServiceTokens.impl.user, userToken).send(MicroServicePorts.user.APIUrl).get
  }
}
