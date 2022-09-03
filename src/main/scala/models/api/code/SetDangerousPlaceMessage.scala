package models.api.code

import com.typesafe.scalalogging.Logger
import models.Trace
import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import models.enums.RiskLevel
import org.joda.time.DateTime

import scala.util.Try

case class SetDangerousPlaceMessage(userToken: String, place: Trace, level: RiskLevel) extends TSMSPMessage {
  val LOGGER = Logger("SetDangerousPlaceMessage")

  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    LOGGER.info(s"place = ${place}, level = ${level.value}");
    TSMSPReply(HandleStatus.OK, dangerousUpdate(userToken, place, level, now).get)
  }
}
