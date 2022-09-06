package api.trace.withPeople

import scala.util.Try
import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken, UserName}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserAddTraceWithPeopleMessage(userToken: String, idCard: IDCard, cc: UserName) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, addTraceWithPeople(userToken, idCard, cc, now).get)
//  }
}
