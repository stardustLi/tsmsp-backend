package models.api.code

import models.api.{HandleStatus, TSMSPMessage, TSMSPReply}
import org.joda.time.DateTime
import services.CodeService.queryJingReports

import scala.util.Try

case class QueryJingReportsMessage(userToken: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, queryJingReports(userToken, now).get)
  }
}
