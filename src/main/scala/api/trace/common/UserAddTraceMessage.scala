package api.trace.common

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserAddTraceMessage(userToken: String, idCard: IDCard, trace: TraceID) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, addTrace(userToken, idCard, trace, now).get)
//  }
}
