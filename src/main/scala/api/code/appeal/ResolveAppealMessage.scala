package api.code.appeal

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.AdminPermission
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class ResolveAppeal(secret: MicroServiceToken, idCard: IDCard) extends ExoticMessage

case class ResolveAppealMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.VIEW_APPEALS)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    ResolveAppeal(MicroServiceTokens.impl.code, idCard)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
