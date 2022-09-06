package api.trace

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields._
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetPlaceInfo(secret: MicroServiceToken, traceID: TraceID, `type`: String = "GetPlaceInfo") extends JacksonSerializable

case class GetPlaceInfoMessage(traceID: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetPlaceInfo(MicroServiceTokens.impl.trace, traceID)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
