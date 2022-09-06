package api.nucleicAcidTest

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, NucleicAcidTestPointName, TraceID}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddNucleicAcidTestPointMessage(userToken: String, place: TraceID, name: NucleicAcidTestPointName) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, addNucleicAcidTestPoint(userToken, place, name, now).get)
//  }
}
