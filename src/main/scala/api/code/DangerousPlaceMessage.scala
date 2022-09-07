package api.code

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class DangerousQuery(secret: MicroServiceToken, place: TraceID) extends ExoticMessage

case class DangerousPlaceMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    DangerousQuery(MicroServiceTokens.impl.code, place)
      .send[TSMSPReply](MicroServicePorts.code.APIUrl)
      .get
  }
}
