package api.code.appeal

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAccessPermission
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddAppeal(secret: MicroServiceToken, idCard: IDCard, reason: String) extends ExoticMessage

case class UserAppealMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AddAppeal(MicroServiceTokens.impl.code, idCard, reason)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
