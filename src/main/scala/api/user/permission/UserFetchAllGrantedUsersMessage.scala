package api.user.permission

import scala.util.Try

import api.TSMSPMessage
import models.fields.MicroServiceToken
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class FetchAllGrantedUsers(secret: MicroServiceToken, userToken: String) extends ExoticMessage

case class UserFetchAllGrantedUsersMessage(userToken: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    FetchAllGrantedUsers(MicroServiceTokens.impl.user, userToken)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl)
      .get
  }
}
