package api.code

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.{AdminPermission, CodeColor}
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AdminSetColor(secret: MicroServiceToken, idCard: IDCard, color: CodeColor) extends ExoticMessage

case class AdminSetColorMessage(userToken: String, idCard: IDCard, color: CodeColor) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.ASSIGN_COLOR)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AdminSetColor(MicroServiceTokens.impl.code, idCard, color)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
