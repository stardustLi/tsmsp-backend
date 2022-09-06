package api.trace.common

import scala.util.{Success, Try}

import api.{TSMSPMessage, TSMSPReply}
import api.exotic._
import models.fields._
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class DeleteTrace(secret: MicroServiceToken, idCard: IDCard, time: Long, `type`: String = "DeleteTrace") extends JacksonSerializable

case class UserDeleteTraceMessage(userToken: String, idCard: IDCard, time: Long) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    DeleteTrace(MicroServiceTokens.impl.trace, idCard, time)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
