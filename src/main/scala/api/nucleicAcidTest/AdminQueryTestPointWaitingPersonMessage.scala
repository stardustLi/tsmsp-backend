package api.nucleicAcidTest

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{MicroServiceToken, NucleicAcidTestPointName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AdminQueryTestPointWaitingPersonMessage(userToken: String, place: NucleicAcidTestPointName) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, queryWaitingPerson(userToken, place, now).get)
//  }
}
