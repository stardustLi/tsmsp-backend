package api.policy

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Query(secret: MicroServiceToken, place: TraceID, `type`: String = "Query") extends JacksonSerializable

case class PolicyQueryMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    Query(MicroServiceTokens.impl.trace, place).send[TSMSPReply](MicroServicePorts.trace.APIUrl).get
  }
}
