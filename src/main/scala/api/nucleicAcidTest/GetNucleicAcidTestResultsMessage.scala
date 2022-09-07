package api.nucleicAcidTest

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAccessPermission
import models.fields.{IDCard, MicroServiceToken}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetNucleicAcidTestResults(secret: MicroServiceToken, idCard: IDCard) extends ExoticMessage

case class GetNucleicAcidTestResultsMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
      case Success(response) if response.status == 0 =>
      case other => return other
    }
    GetNucleicAcidTestResults(MicroServiceTokens.impl.nucleicAcidTest, idCard)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
