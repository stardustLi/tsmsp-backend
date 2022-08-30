package models.api

import com.typesafe.scalalogging.Logger
import models.enums.RiskLevel
import models.{HandleStatus, TSMSPReply, Trace}
import org.joda.time.DateTime
import services.DangerousPlaceService.dangerousUpdate

import scala.util.Try

case class SetDangerousPlaceMessage(userToken: String, place: Trace, level: RiskLevel) extends TSMSPMessage {
  val LOGGER = Logger("SetDangerousPlaceMessage")

  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    LOGGER.info(s"place = ${place}, level = ${level.value}");
    TSMSPReply(HandleStatus.OK, dangerousUpdate(userToken, place, level, now).get)
  }
}
