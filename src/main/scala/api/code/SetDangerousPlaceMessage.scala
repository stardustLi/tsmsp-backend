package api.code

import scala.util.Try

import api.TSMSPMessage
import models.enums.RiskLevel
import models.fields.{MicroServiceToken, TraceID}
import models.types.{JacksonSerializable, TSMSPReply}
import utils.{MicroServicePorts, MicroServiceTokens}
import utils.MicroServicePorts.Port
import utils.http.sender

case class SetDangerousPlaceMessage(userToken: String, place: TraceID, level: RiskLevel) extends TSMSPMessage {
//  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
//    TSMSPReply(HandleStatus.OK, dangerousUpdate(userToken, place, level, now).get)
//  }
}
