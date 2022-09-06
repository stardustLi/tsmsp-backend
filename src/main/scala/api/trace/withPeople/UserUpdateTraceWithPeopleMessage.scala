package api.trace.withPeople

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken, UserName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserUpdateTraceWithPeopleMessage(userToken: String, idCard: IDCard, time: Long, cc: UserName) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, updateTraceWithPeople(userToken, idCard, time, cc, now).get)
//  }
}
