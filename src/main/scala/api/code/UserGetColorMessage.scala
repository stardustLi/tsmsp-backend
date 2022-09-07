package api.code

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAccessPermission
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserGetColor(secret: MicroServiceToken, idCard: IDCard) extends ExoticMessage

case class UserGetColorMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    UserGetColor(MicroServiceTokens.impl.code, idCard)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
