package api.nucleicAcidTest

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.AdminPermission
import models.fields.{MicroServiceToken, NucleicAcidTestPointName}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AdminQueryTestPointWaitingPerson(secret: MicroServiceToken, place: NucleicAcidTestPointName) extends ExoticMessage

case class AdminQueryTestPointWaitingPersonMessage(userToken: String, place: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.FINISH_NUCLEIC_ACID_TEST)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AdminQueryTestPointWaitingPerson(MicroServiceTokens.impl.nucleicAcidTest, place)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
