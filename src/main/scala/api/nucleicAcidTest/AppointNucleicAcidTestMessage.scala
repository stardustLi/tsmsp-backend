package api.nucleicAcidTest

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken, NucleicAcidTestPointName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AppointNucleicAcidTestMessage(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, appointNucleicAcidTest(userToken, idCard, testPlace, now).get)
//  }
}
