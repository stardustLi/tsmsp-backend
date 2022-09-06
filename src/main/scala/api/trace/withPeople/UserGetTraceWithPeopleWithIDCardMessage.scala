package api.trace.withPeople

import scala.util.{Success, Try}

import api.{TSMSPMessage, TSMSPReply}
import api.exotic._
import models.enums._
import models.fields._
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetTraceWithPeopleWithIDCard(secret: MicroServiceToken, idCard: IDCard, startTime: Long, endTime: Long, `type`: String = "GetTraceWithPeopleWithIDCard") extends JacksonSerializable

case class UserGetTraceWithPeopleWithIDCardMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAdminPermission(MicroServiceTokens.impl.user, userToken, AdminPermission.READ_TRACE_ID)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
      case Success(response) if response.status == 0 =>
      case other => return other
    }
    GetTraceWithPeopleWithIDCard(MicroServiceTokens.impl.trace, idCard, startTime, endTime)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
