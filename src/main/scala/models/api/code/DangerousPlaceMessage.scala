package models.api.code

import org.joda.time.DateTime
import scala.util.Try

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.fields.TraceID
import services.CodeService.dangerousQuery

case class DangerousPlaceMessage(place: TraceID) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, dangerousQuery(place).get)
  }
}
