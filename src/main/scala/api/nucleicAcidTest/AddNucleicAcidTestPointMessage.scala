package api.nucleicAcidTest

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic.CheckAdminPermission
import models.enums.AdminPermission
import models.fields.{MicroServiceToken, NucleicAcidTestPointName, TraceID}
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddNucleicAcidTestPoint(secret: MicroServiceToken, place: TraceID, name: NucleicAcidTestPointName) extends ExoticMessage

case class AddNucleicAcidTestPointMessage(userToken: String, place: TraceID, name: NucleicAcidTestPointName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.MANAGE_NUCLEIC_ACID_TEST_POINTS)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AddNucleicAcidTestPoint(MicroServiceTokens.impl.nucleicAcidTest, place, name)
      .send[TSMSPReply](MicroServicePorts.nucleicAcidTest.APIUrl)
      .get
  }
}
