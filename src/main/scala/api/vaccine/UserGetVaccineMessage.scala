package api.vaccine

import scala.util.{Success, Try}

import api.exotic.CheckAccessPermission
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetVaccine(secret: MicroServiceToken, idCard: IDCard, `type`: String = "GetVaccine") extends JacksonSerializable

case class UserGetVaccineMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
      case Success(response) if response.status == 0 =>
      case other => return other
    }
    GetVaccine(MicroServiceTokens.impl.vaccine, idCard)
      .send[TSMSPReply](MicroServicePorts.vaccine.APIUrl)
      .get
  }
}
