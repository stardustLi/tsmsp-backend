package api.policy

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class Query(secret: MicroServiceToken, place: TraceID) extends ExoticMessage

case class PolicyQueryMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    Query(MicroServiceTokens.impl.policy, place)
      .send[TSMSPReply](MicroServicePorts.policy.APIUrl)
      .get
  }
}
