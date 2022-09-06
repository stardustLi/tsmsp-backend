package api.trace

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.MicroServiceToken
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class CreatePlaceMessage(userToken: String, traceDescriptor: List[String]) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, createPlace(userToken, traceDescriptor, now).get)
//  }
}
