package api.trace.withPeople

import scala.util.{Success, Try}

import api.{TSMSPMessage, TSMSPReply}
import api.exotic._
import models.fields._
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AddTraceWithPeople(secret: MicroServiceToken, idCard: IDCard, cc: UserName, `type`: String = "AddTraceWithPeople") extends JacksonSerializable

case class UserAddTraceWithPeopleMessage(userToken: String, idCard: IDCard, cc: UserName) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    AddTraceWithPeople(MicroServiceTokens.impl.trace, idCard, cc)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
