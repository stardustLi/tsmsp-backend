package api.vaccine

import scala.util.{Success, Try}

import api.exotic.CheckAccessPermission
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddVaccine(secret: MicroServiceToken, idCard: IDCard, manufacture: String, time: Long, `type`: String = "AddVaccine") extends JacksonSerializable

case class UserAddVaccineMessage(userToken: String, idCard: IDCard, manufacture: String, time: Long) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
      case Success(response) if response.status == 0 =>
      case other => return other
    }
    AddVaccine(MicroServiceTokens.impl.vaccine, idCard, manufacture, time)
      .send[TSMSPReply](MicroServicePorts.vaccine.APIUrl)
      .get
  }
}
