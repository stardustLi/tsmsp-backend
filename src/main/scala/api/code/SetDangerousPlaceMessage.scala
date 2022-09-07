package api.code

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.{AdminPermission, RiskLevel}
import models.fields.{MicroServiceToken, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class DangerousUpdate(secret: MicroServiceToken, place: TraceID, level: RiskLevel) extends ExoticMessage

case class SetDangerousPlaceMessage(userToken: String, place: TraceID, level: RiskLevel) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.SET_RISK_AREAS)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    DangerousUpdate(MicroServiceTokens.impl.code, place, level)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
