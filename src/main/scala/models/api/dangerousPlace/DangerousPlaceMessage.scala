package models.api.dangerousPlace

import models.Trace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.DangerousPlaceService.dangerousQuery

import scala.util.Try

case class DangerousPlaceMessage(place: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, dangerousQuery(place).get)
  }
}
