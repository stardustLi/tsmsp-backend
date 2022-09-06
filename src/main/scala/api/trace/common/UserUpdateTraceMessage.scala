package api.trace.common

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserUpdateTraceMessage(userToken: String, idCard: IDCard, time: Long, trace: TraceID) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, updateTrace(userToken, idCard, time, trace, now).get)
//  }
}
