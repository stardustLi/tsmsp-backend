package api.trace.withPeople

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserGetTraceWithPeopleMessage(userToken: String, idCard: IDCard, startTime: Long, endTime: Long) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, apiGetTracesWithPeople(userToken, idCard, startTime, endTime, now).get)
//  }
}
