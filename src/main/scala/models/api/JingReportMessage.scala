package models.api

import models.fields.IDCard
import models.{HandleStatus, TSMSPReply}
import org.joda.time.DateTime
import services.CodeService.jingReport

import scala.util.Try

case class JingReportMessage(userToken: String, idCard: IDCard, reason: String) extends TSMSPMessage {
  override def reaction(now: DateTime): Try[TSMSPReply] = Try {
    TSMSPReply(HandleStatus.OK, jingReport(userToken, idCard, reason, now).get)
  }
}
