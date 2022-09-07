package api.code

import scala.util.Try

import api.TSMSPMessage
import models.enums.CodeColor
import models.fields.{IDCard, MicroServiceToken}
import models.types.{JacksonSerializable, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class AdminSetColorMessage(userToken: String, idCard: IDCard, color: CodeColor) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, adminSetColor(userToken, idCard, color, now).get)
//  }
}
