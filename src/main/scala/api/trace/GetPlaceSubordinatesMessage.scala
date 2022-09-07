package api.trace

import scala.util.Try

import api.TSMSPMessage
import models.fields._
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetPlaceSubordinates(secret: MicroServiceToken, traceID: TraceID) extends ExoticMessage

case class GetPlaceSubordinatesMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetPlaceSubordinates(MicroServiceTokens.impl.trace, traceID)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
