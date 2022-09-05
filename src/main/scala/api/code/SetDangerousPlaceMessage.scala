package api.code

import api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime

import scala.util.Try
import models.fields.TraceID
import models.enums.RiskLevel
import services.CodeService.dangerousUpdate

case class SetDangerousPlaceMessage(userToken: String, place: TraceID, level: RiskLevel) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, dangerousUpdate(userToken, place, level, now).get)
  }
}
