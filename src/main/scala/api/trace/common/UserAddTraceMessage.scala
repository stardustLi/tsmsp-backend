package api.trace.common

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic._
import models.fields._
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddTrace(secret: MicroServiceToken, idCard: IDCard, trace: TraceID) extends ExoticMessage

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AddTrace(MicroServiceTokens.impl.trace, idCard, trace)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
