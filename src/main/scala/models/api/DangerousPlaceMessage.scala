package models.api

import models.{HandleStatus, TSMSPReply, Trace}
import org.joda.time.DateTime
import service.DangerousPlaceService.dangerousQuery

import scala.util.Try

case class DangerousPlaceMessage(place: Trace) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, dangerousQuery(place).get)
  }
}