package api.nucleicAcidTest

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, NucleicAcidTestPointName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class QueryTestPointWaitingPerson(secret: MicroServiceToken, place: NucleicAcidTestPointName) extends ExoticMessage

case class QueryTestPointWaitingPersonMessage(place: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    QueryTestPointWaitingPerson(MicroServiceTokens.impl.nucleicAcidTest, place)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
