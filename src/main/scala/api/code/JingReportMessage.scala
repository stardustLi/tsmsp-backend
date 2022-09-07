package api.code

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAccessPermission
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddJingReport(secret: MicroServiceToken, idCard: IDCard, reason: String) extends ExoticMessage

case class JingReportMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AddJingReport(MicroServiceTokens.impl.code, idCard, reason)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
