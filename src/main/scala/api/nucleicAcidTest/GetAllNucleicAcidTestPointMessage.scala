package api.nucleicAcidTest

import scala.util.Try

import api.TSMSPMessage
import models.fields.{MicroServiceToken, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetAllNucleicAcidTestPoint(secret: MicroServiceToken, place: TraceID) extends ExoticMessage

case class GetAllNucleicAcidTestPointMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    GetAllNucleicAcidTestPoint(MicroServiceTokens.impl.nucleicAcidTest, place)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
