package api.nucleicAcidTest

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetNucleicAcidTestResultsMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, getNucleicAcidTests(userToken, idCard, now).get)
//  }
}
