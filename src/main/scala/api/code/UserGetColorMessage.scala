package api.code

import scala.util.Try

import api.{TSMSPMessage, TSMSPReply}
import models.fields.{IDCard, MicroServiceToken}
import models.types.JacksonSerializable
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserGetColorMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, getColor(userToken, idCard, now).get)
//  }
}
