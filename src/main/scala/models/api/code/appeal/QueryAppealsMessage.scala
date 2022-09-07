package models.api.code.appeal

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.CodeService.queryAppeals

import scala.util.Try

case class QueryAppealsMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryAppeals(userToken, now).get)
  }
}
