package api.nucleicAcidTest

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetAllNucleicAcidTestPointMessage(place: TraceID) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, getAllNucleicAcidTestPoints(place).get)
//  }
}
