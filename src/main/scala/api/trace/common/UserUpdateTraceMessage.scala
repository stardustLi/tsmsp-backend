package api.trace.common

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic._
import models.fields._
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UpdateTrace(secret: MicroServiceToken, idCard: IDCard, time: Long, trace: TraceID) extends ExoticMessage

case class UserUpdateTraceMessage(userToken: String, idCard: IDCard, time: Long, trace: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    UpdateTrace(MicroServiceTokens.impl.trace, idCard, time, trace)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
