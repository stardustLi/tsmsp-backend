package api.code.appeal

import scala.util.Try

import api.TSMSPMessage
import models.fields.{IDCard, MicroServiceToken}
import models.types.{JacksonSerializable, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class QueryAppealMessage(userToken: String, idCard: IDCard) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, queryAppeal(userToken, idCard, now).get)
//  }
}
