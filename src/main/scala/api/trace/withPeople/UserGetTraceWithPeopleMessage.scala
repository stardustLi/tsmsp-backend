package api.trace.withPeople

import scala.util.{Success, Try}

import api.TSMSPMessage
import api.exotic._
import models.fields._
import models.types.{ExoticMessage, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class GetTraceWithPeople(secret: MicroServiceToken, idCard: IDCard, startTime: Long, endTime: Long) extends ExoticMessage

case class UserGetTraceWithPeopleMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
  override def reaction(): Try[TSMSPReply] = Try {
    CheckAccessPermission(MicroServiceTokens.impl.user, userToken, idCard)
      .send[TSMSPReply](MicroServicePorts.user.APIUrl) match {
        case Success(response) if response.status == 0 =>
        case other => return other
      }
    GetTraceWithPeople(MicroServiceTokens.impl.trace, idCard, startTime, endTime)
      .send[TSMSPReply](MicroServicePorts.trace.APIUrl)
      .get
  }
}
