package api.code

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.{TraceID, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class DangerousPlaceMessage(place: TraceID) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, dangerousQuery(place).get)
//  }
}
