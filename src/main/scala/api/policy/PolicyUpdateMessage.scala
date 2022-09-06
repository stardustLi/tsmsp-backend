package api.policy

import scala.util.{Success, Try}

import api.{TSMSPMessage, TSMSPReply}
import models.enums.AdminPermission
import models.fields.{MicroServiceToken, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class CheckAdminPermission(secret: MicroServiceToken, token: String, field: AdminPermission, `type`: String = "CheckAdminPermission") extends JacksonSerializable
case class Update(secret: MicroServiceToken, place: TraceID, content: String, `type`: String = "Update") extends JacksonSerializable

case class PolicyUpdateMessage(userToken: String, place: TraceID, content: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.SET_POLICY)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    Update(MicroServiceTokens.impl.trace, place, content).send[TSMSPReply](MicroServicePorts.trace.APIUrl).get
  }
}
