package api.code.appeal

import scala.util.Try

import api.TSMSPMessage
import models.fields.{IDCard, MicroServiceToken}
import models.types.{JacksonSerializable, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class UserAppealMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, addAppeal(userToken, idCard, reason, now).get)
//  }
}
