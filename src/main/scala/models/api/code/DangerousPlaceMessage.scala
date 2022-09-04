package models.api.code

import org.joda.time.DateTime
import scala.util.Try

import models.Trace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import services.CodeService.dangerousQuery

case class DangerousPlaceMessage(place: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, dangerousQuery(place).get)
  }
}
