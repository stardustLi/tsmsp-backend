package api.nucleicAcidTest

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAccessPermission
import models.fields.{IDCard, MicroServiceToken, NucleicAcidTestPointName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AppointNucleicAcidTest(secret: MicroServiceToken, idCard: IDCard, testPlace: NucleicAcidTestPointName) extends ExoticMessage

case class AppointNucleicAcidTestMessage(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
      case Success(response) if response.status == 0 =>
      case other => return other
    }
    AppointNucleicAcidTest(MicroServiceTokens.impl.nucleicAcidTest, idCard, testPlace)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
