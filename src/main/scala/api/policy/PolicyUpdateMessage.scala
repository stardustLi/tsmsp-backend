package api.policy

import scala.util.{Success, Try}

import api.exotic.CheckAdminPermission
import api.TSMSPMessage
import models.enums.AdminPermission
import models.fields.{MicroServiceToken, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Update(secret: MicroServiceToken, place: TraceID, content: String) extends ExoticMessage

case class PolicyUpdateMessage(userToken: String, place: TraceID, content: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.SET_POLICY)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    Update(MicroServiceTokens.impl.policy, place, content)
      .send[TSMSPReply](MicroServicePorts.policy.APIUrl)
      .get
  }
}
